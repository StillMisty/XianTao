package top.stillmisty.xiantao.service.combat;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingStartResult;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.user.enums.CultivationRealm;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.activity.TrainingCompleter;
import top.stillmisty.xiantao.service.ai.ExplorationDescriptionFunction;
import top.stillmisty.xiantao.service.player.PlayerLoader;
import top.stillmisty.xiantao.service.player.PlayerWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

  private final PlayerLoader playerLoader;
  private final PlayerWriter playerWriter;
  private final MapNodeRepository mapNodeRepository;
  private final TrainingCompleter trainingCompleter;
  private final ExplorationDescriptionFunction explorationDescriptionFunction;
  private final TrainingSettler trainingSettler;
  private final TransactionTemplate transactionTemplate;

  // 事务注解必须放在外部调用的 public 方法上，标注在 Internal 方法会因自调用绕过代理而失效
  @Transactional
  public ServiceResult<TrainingStartResult> startTraining(Long userId) {
    return new ServiceResult.Success<>(startTrainingInternal(userId));
  }

  /** 历练结算：结算与落库在短事务内完成，LLM 叙述在事务提交后执行，避免长持行锁。 */
  public ServiceResult<TrainingRewardVO> endTraining(Long userId) {
    return new ServiceResult.Success<>(endTrainingFlow(userId));
  }

  // ===================== 内部 API =====================

  public TrainingStartResult startTrainingInternal(Long userId) {
    Player user = playerLoader.load(userId);
    if (user.getStatus() != UserStatus.IDLE) {
      throw new BusinessException(ErrorCode.STATUS_BLOCKED, user.getStatus().getName(), "空闲");
    }
    if (user.getLocationId() == null) {
      return TrainingStartResult.builder().success(false).message("当前位置无效，无法开始历练").build();
    }
    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElse(null);
    if (mapNode == null) {
      return TrainingStartResult.builder().success(false).message("当前地图不存在，无法开始历练").build();
    }
    if (mapNode.getMapType() != MapType.TRAINING_ZONE) {
      return TrainingStartResult.builder()
          .success(false)
          .message(buildNotTrainingZoneMessage(mapNode))
          .build();
    }
    user.beginActivity(ActivityType.TRAINING, UserStatus.TRAINING, TimeUtil.now(), mapNode.getId());
    playerWriter.saveActivity(user);
    log.info("玩家 {} 开始在 {} 历练", userId, mapNode.getName());
    return TrainingStartResult.builder().success(true).mapName(mapNode.getName()).build();
  }

  private TrainingRewardVO endTrainingFlow(Long userId) {
    TrainingOutcome outcome = transactionTemplate.execute(status -> endTrainingInternal(userId));
    if (outcome == null) {
      throw new IllegalStateException("历练结算事务未返回结果: " + userId);
    }
    outcome.vo().setSummary(beautifyOutcome(outcome));
    return outcome.vo();
  }

  /** 结算结果：VO 附带叙述所需上下文（LLM 调用须在事务提交后进行） */
  private record TrainingOutcome(
      TrainingRewardVO vo,
      @Nullable MapNode mapNode,
      long minutesTraining,
      long totalExp,
      List<String> itemNames,
      CombatSummary combatSummary,
      @Nullable String combatHighlight,
      @Nullable String defeatNarrative,
      @Nullable String beastNarrative) {}

  private TrainingOutcome endTrainingInternal(Long userId) {
    Player user = playerLoader.load(userId);
    if (user.getStatus() != UserStatus.TRAINING && user.getStatus() != UserStatus.DYING) {
      throw new BusinessException(ErrorCode.STATUS_BLOCKED, user.getStatus().getName(), "历练");
    }
    TrainingRewardVO earlyResult = checkEndTrainingEarlyExit(userId, user);
    if (earlyResult != null) {
      return new TrainingOutcome(
          earlyResult, null, 0, 0, List.of(), CombatSummary.empty(), null, null, null);
    }

    long minutesTraining =
        Duration.between(user.getActivityStartTime(), TimeUtil.now()).toMinutes();
    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();
    return processNormalTrainingEnd(userId, user, minutesTraining, mapNode);
  }

  @Nullable
  private TrainingRewardVO checkEndTrainingEarlyExit(Long userId, Player user) {
    if (user.getActivityStartTime() == null) {
      user.clearActivity();
      playerWriter.saveActivity(user);
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("您当前没有在历练")
          .build();
    }
    long minutesTraining =
        Duration.between(user.getActivityStartTime(), TimeUtil.now()).toMinutes();
    if (minutesTraining <= 5) {
      user.clearActivity();
      playerWriter.saveActivity(user);
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("历练时间过短毫无收获")
          .build();
    }
    if (mapNodeRepository.findById(user.getLocationId()).isEmpty()) {
      user.clearActivity();
      playerWriter.saveActivity(user);
      return TrainingRewardVO.builder().userId(userId).summary("当前地图不存在").build();
    }
    return null;
  }

  private TrainingOutcome processNormalTrainingEnd(
      Long userId, Player user, long minutesTraining, MapNode mapNode) {
    long lastSettled = user.getLastSettlementMinute();

    // 结算全部未结算时长：基础修为、物品、事件循环与灵兽经验统一在结算器内完成
    TrainingSettlement settlement =
        trainingSettler.settle(userId, user, mapNode, lastSettled, minutesTraining);

    boolean diedInTraining = user.getStatus() == UserStatus.DYING;
    CombatSummary combatSummary = settlement.combatSummary();
    List<DropItem> trainingItems = settlement.items();
    long totalExp = settlement.totalExp();

    trainingCompleter.checkHiddenEvents(userId, user, mapNode);

    if (diedInTraining) {
      user.endActivity();
      trainingCompleter.produceInterruptedEvent(userId, mapNode);
    } else {
      user.clearActivity();
      trainingCompleter.produceCompletionEvent(
          userId, user, mapNode, minutesTraining, totalExp, trainingItems.size());
      trainingCompleter.applyEnvironmentalEvents(userId, user, mapNode);
    }
    playerWriter.saveTrainingEndState(user);

    List<String> itemNames =
        trainingItems.stream().map(DropItem::name).filter(Objects::nonNull).toList();
    @Nullable String combatHighlight = buildHighlightBattleText(combatSummary);
    @Nullable String defeatNarrative = diedInTraining ? buildDefeatNarrative(combatSummary) : null;
    @Nullable String beastNarrative =
        diedInTraining && settlement.beastDeployed() ? buildBeastNarrative(combatSummary) : null;
    String plainSummary =
        buildEndTrainingSummary(
            minutesTraining,
            totalExp,
            combatSummary,
            trainingItems,
            diedInTraining,
            defeatNarrative,
            beastNarrative);

    log.info("玩家 {} 结束历练并应用奖励", userId);
    TrainingRewardVO vo =
        TrainingRewardVO.builder()
            .userId(userId)
            .mapId(mapNode.getId())
            .mapName(mapNode.getName())
            .durationMinutes(minutesTraining)
            .efficiencyMultiplier(settlement.efficiencyMultiplier())
            .levelDecayMultiplier(settlement.levelDecayMultiplier())
            .exp(totalExp)
            .items(trainingItems)
            .summary(plainSummary)
            .build();
    return new TrainingOutcome(
        vo,
        mapNode,
        minutesTraining,
        totalExp,
        itemNames,
        combatSummary,
        combatHighlight,
        defeatNarrative,
        beastNarrative);
  }

  /** LLM 叙述（须在事务提交后调用）。失败时由叙述模块内部兜底为结算原文。 */
  private String beautifyOutcome(TrainingOutcome o) {
    if (o.mapNode() == null || o.vo().getSummary() == null) {
      return o.vo().getSummary();
    }
    var request =
        new ExplorationDescriptionFunction.Request(
            o.mapNode().getName(),
            o.mapNode().getDescription(),
            "历时" + o.minutesTraining() + "分钟的野外历练",
            o.itemNames(),
            o.totalExp() > 0 ? o.totalExp() : null,
            null,
            buildCombatSummaryText(o.combatSummary()),
            o.combatHighlight(),
            o.defeatNarrative(),
            o.beastNarrative());
    return explorationDescriptionFunction.beautify(request).description();
  }

  // ===================== 物品与修为计算 =====================

  private String buildCombatSummaryText(CombatSummary cs) {
    if (cs.totalEncounters() == 0) return "";
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("遇敌%d场 | 击杀%d只", cs.totalEncounters(), cs.totalKills()));
    if (cs.defeatCount() > 0) sb.append(String.format(" | 战败%d场", cs.defeatCount()));
    if (cs.expGained() > 0) sb.append(String.format(" | 修为+%d", cs.expGained()));
    return sb.toString();
  }

  @Nullable
  private String buildHighlightBattleText(CombatSummary cs) {
    if (!cs.hasHighlight() || cs.firstHighlightLogs().isEmpty()) return null;
    StringBuilder sb = new StringBuilder();
    sb.append("你遭遇了").append(cs.firstHighlightMonsterName()).append("，这是一场苦战：\n");
    for (var entry : cs.firstHighlightLogs()) {
      sb.append(String.format("  [第%d回合] ", entry.round()));
      sb.append(entry.attackerName()).append(" ");
      sb.append(entry.attackType() == CombatLogEntry.AttackType.SKILL ? "施展" : "攻击");
      if (entry.skillName() != null && !entry.skillName().isEmpty()) {
        sb.append("「").append(entry.skillName()).append("」");
      }
      sb.append(" → ").append(entry.defenderName());
      if (entry.damageDealt() > 0) {
        sb.append(String.format("（%d点伤害", entry.damageDealt()));
        sb.append("，气血 ")
            .append(entry.defenderHpBefore())
            .append(" → ")
            .append(entry.defenderHpAfter());
        sb.append("）");
      }
      if (entry.isKill()) {
        sb.append(" 击杀！");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private String buildEndTrainingSummary(
      long minutesTraining,
      long totalExp,
      CombatSummary combatSummary,
      List<DropItem> trainingItems,
      boolean diedInTraining,
      @Nullable String defeatNarrative,
      @Nullable String beastNarrative) {
    StringBuilder summary = new StringBuilder();
    summary.append(String.format("历练时长: %d 分钟\n", minutesTraining));
    if (totalExp > 0) summary.append(String.format("修为: +%d\n", totalExp));
    if (combatSummary.totalEncounters() > 0) {
      summary.append(buildCombatSummaryText(combatSummary)).append("\n");
    }
    if (!trainingItems.isEmpty()) {
      summary.append("物品:\n");
      for (DropItem item : trainingItems) {
        summary.append(String.format("  %s x%d\n", item.name(), item.quantity()));
      }
    }
    if (defeatNarrative != null) {
      summary.append("\n").append(defeatNarrative).append("\n");
    }
    if (beastNarrative != null) {
      summary.append(beastNarrative).append("\n");
    }
    if (diedInTraining) {
      summary.append("\n你力战至脱力昏迷，30 分钟后自愈苏醒。重伤前所获之物已收入囊中。");
    }
    return summary.toString();
  }

  @Nullable
  private String buildDefeatNarrative(CombatSummary cs) {
    if (cs.lastDefeatMonsterName() == null) return null;
    var logs = cs.lastDefeatLogs();
    if (logs == null || logs.isEmpty()) {
      return "你遭遇了" + cs.lastDefeatMonsterName() + "，一场恶战后不敌落败。";
    }
    // 取最后一条有效日志作为"致命一击"
    CombatLogEntry killingBlow = logs.getLast();
    if (killingBlow.damageDealt() > 0) {
      String skillPart =
          killingBlow.attackType() == CombatLogEntry.AttackType.SKILL
                  && killingBlow.skillName() != null
                  && !killingBlow.skillName().isEmpty()
              ? "一记「" + killingBlow.skillName() + "」"
              : "凌厉一击";
      return killingBlow.attackerName() + skillPart + "正中你的要害，你眼前一黑，重伤倒地。";
    }
    return "你与" + cs.lastDefeatMonsterName() + "血战数十回合，终因力竭不敌，重伤倒地。";
  }

  @Nullable
  private String buildBeastNarrative(CombatSummary cs) {
    if (!cs.hasHighlight()) return null;
    // 高光战斗中如果有 BeastCombatant 参与了，由 LLM 自行生成；这里给 fallback
    return "你的灵兽一直相伴左右并肩而战，在你昏迷前奋力将你拖出了险境。";
  }

  /** 非历练区报错文案 — BFS 指引最近的历练区 */
  private String buildNotTrainingZoneMessage(MapNode current) {
    MapNode nearest = findNearestTrainingZone(current.getId());
    if (nearest == null) {
      return "「" + current.getName() + "」灵气枯竭，无处可修炼，还是另寻洞天吧";
    }
    return "「"
        + current.getName()
        + "」人烟稠密，不宜吐纳。可先「前往 "
        + nearest.getName()
        + "」（其妖兽多为"
        + CultivationRealm.realmDisplay(nearest.getLevelRequirement())
        + "修为），抵达后再行修炼。";
  }

  /** 广度优先搜索距离最近的历练区 */
  @Nullable
  private MapNode findNearestTrainingZone(Long startId) {
    List<MapNode> allNodes = mapNodeRepository.findAll();
    Map<Long, MapNode> nodeMap =
        allNodes.stream().collect(Collectors.toMap(MapNode::getId, n -> n));
    Set<Long> visited = new HashSet<>();
    Queue<Long> queue = new ArrayDeque<>();
    queue.add(startId);
    visited.add(startId);
    while (!queue.isEmpty()) {
      MapNode node = nodeMap.get(queue.poll());
      if (node == null) continue;
      for (Long adjacentId : node.getAdjacentMapIds()) {
        if (!visited.add(adjacentId)) continue;
        MapNode adjacent = nodeMap.get(adjacentId);
        if (adjacent != null && adjacent.getMapType() == MapType.TRAINING_ZONE) {
          return adjacent;
        }
        queue.add(adjacentId);
      }
    }
    return null;
  }
}
