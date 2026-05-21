package top.stillmisty.xiantao.service.combat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.repository.ActivityEventRepository;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.map.vo.TrainingRewardVO;
import top.stillmisty.xiantao.domain.map.vo.TrainingStartResult;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.repository.MonsterTemplateRepository;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.util.TypeUtils;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.activity.TrainingCompleter;
import top.stillmisty.xiantao.service.ai.ExplorationDescriptionFunction;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

  private static final double AGILITY_EFFICIENCY_COEFFICIENT = 0.01;
  private static final double MAX_EFFICIENCY_BOOST = 2.0;
  private static final double LEVEL_DECAY_RATE = 0.04;
  private static final double MIN_LEVEL_DECAY_MULTIPLIER = 0.1;
  private static final int LEVEL_DECAY_OFFSET = 5;

  private final UserStateService userStateService;
  private final MapNodeRepository mapNodeRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final TrainingCompleter trainingCompleter;
  private final ExplorationDescriptionFunction explorationDescriptionFunction;
  private final ActivityEventRepository activityEventRepository;
  private final EncounterCalculator encounterCalculator;
  private final CombatEventHandler combatEventHandler;
  private final MonsterTemplateRepository monsterTemplateRepository;
  private final SkillRepository skillRepository;
  private final GameEventService gameEventService;

  @Authenticated
  @Transactional
  public ServiceResult<TrainingStartResult> startTraining(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(startTraining(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<TrainingRewardVO> endTraining(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(endTraining(userId));
  }

  // ===================== 内部 API =====================

  @Transactional
  public TrainingStartResult startTraining(Long userId) {
    User user = userStateService.loadUserForUpdate(userId);
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
          .message("当前地图不是历练区域（需要野外历练区），无法开始历练")
          .build();
    }
    user.setActivityType(ActivityType.TRAINING);
    user.setActivityStartTime(LocalDateTime.now());
    user.setActivityTargetId(mapNode.getId());
    user.setStatus(UserStatus.TRAINING);
    userStateService.saveActivity(user);
    log.info("玩家 {} 开始在 {} 历练", userId, mapNode.getName());
    return TrainingStartResult.builder().success(true).mapName(mapNode.getName()).build();
  }

  @Transactional
  public TrainingRewardVO endTraining(Long userId) {
    User user = userStateService.loadUserForUpdate(userId);
    if (user.getStatus() != UserStatus.TRAINING && user.getStatus() != UserStatus.DYING) {
      throw new BusinessException(ErrorCode.STATUS_BLOCKED, user.getStatus().getName(), "历练");
    }
    TrainingRewardVO earlyResult = checkEndTrainingEarlyExit(userId, user);
    if (earlyResult != null) return earlyResult;

    long minutesTraining =
        Duration.between(user.getActivityStartTime(), LocalDateTime.now()).toMinutes();
    MapNode mapNode = mapNodeRepository.findById(user.getLocationId()).orElseThrow();
    return processNormalTrainingEnd(userId, user, minutesTraining, mapNode);
  }

  private TrainingRewardVO checkEndTrainingEarlyExit(Long userId, User user) {
    if (user.getStatus() == UserStatus.DYING) {
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("你正处于重伤濒死中，请等待自动恢复（30 分钟后恢复至 20% HP）")
          .build();
    }
    if (user.getActivityStartTime() == null) {
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      userStateService.saveActivity(user);
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("您当前没有在历练")
          .build();
    }
    long minutesTraining =
        Duration.between(user.getActivityStartTime(), LocalDateTime.now()).toMinutes();
    if (minutesTraining <= 5) {
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      userStateService.saveActivity(user);
      return TrainingRewardVO.builder()
          .userId(userId)
          .mapId(user.getLocationId())
          .summary("历练时间过短毫无收获")
          .build();
    }
    if (mapNodeRepository.findById(user.getLocationId()).isEmpty()) {
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      userStateService.saveActivity(user);
      return TrainingRewardVO.builder().userId(userId).summary("当前地图不存在").build();
    }
    return null;
  }

  private TrainingRewardVO processNormalTrainingEnd(
      Long userId, User user, long minutesTraining, MapNode mapNode) {
    double efficiencyMultiplier = calculateEfficiencyMultiplier(user.getEffectiveStatAgi());
    double levelDecayMultiplier =
        calculateLevelDecayMultiplier(user.getLevel(), mapNode.getLevelRequirement());
    long baseExpPerMinute =
        Math.max(
            mapNode.getLevelRequirement() * 5L,
            (long) (Math.sqrt(user.getEffectiveStatWis()) * 12));
    long baseExp =
        (long) (baseExpPerMinute * minutesTraining * efficiencyMultiplier * levelDecayMultiplier);
    List<Map<String, Object>> trainingItems =
        calculateItemsReward(minutesTraining, efficiencyMultiplier, mapNode);

    // 统一事件循环: COMBAT + NUMERIC 一个池子里加权抽
    CombatSummary combatSummary = runUnifiedEventLoop(userId, user, mapNode, (int) minutesTraining);
    boolean diedInTraining = user.getStatus() == UserStatus.DYING;

    // 子事件和隐藏事件优先于基础修为结算（避免溢出）
    trainingCompleter.checkHiddenEvents(userId, user, mapNode);

    if (!diedInTraining && baseExp > 0) {
      user.addExp(baseExp);
    }
    if (!diedInTraining) {
      addTrainingItemsToInventory(userId, trainingItems);
    }
    if (!diedInTraining) {
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      trainingCompleter.produceCompletionEvent(userId, user, mapNode, minutesTraining);
    } else {
      user.endActivity();
      trainingCompleter.produceInterruptedEvent(userId, mapNode);
    }
    userStateService.saveTrainingEndState(user);

    long totalExp = baseExp + combatSummary.expGained();
    List<String> itemNames =
        trainingItems.stream().map(i -> (String) i.get("name")).filter(Objects::nonNull).toList();
    String plainSummary =
        buildEndTrainingSummary(
            minutesTraining, totalExp, combatSummary, trainingItems, diedInTraining);
    String summary =
        beautifyTrainingSummary(
            mapNode, minutesTraining, totalExp, itemNames, combatSummary, plainSummary);

    log.info("玩家 {} 结束历练并应用奖励", userId);
    return TrainingRewardVO.builder()
        .userId(userId)
        .mapId(mapNode.getId())
        .mapName(mapNode.getName())
        .durationMinutes(minutesTraining)
        .efficiencyMultiplier(efficiencyMultiplier)
        .levelDecayMultiplier(levelDecayMultiplier)
        .exp(totalExp)
        .items(trainingItems)
        .summary(summary)
        .build();
  }

  /** 统一事件循环: COMBAT + NUMERIC 同一池子加权随机 */
  private CombatSummary runUnifiedEventLoop(
      Long userId, User user, MapNode mapNode, int minutesTraining) {
    List<ActivityEvent> pool = activityEventRepository.findSubEvents("TRAINING", mapNode.getId());
    if (pool.isEmpty()) return CombatSummary.empty();

    var params = encounterCalculator.compute(userId, user, mapNode, minutesTraining);
    CombatSummary combatSummary = CombatSummary.empty();

    // 预加载 COMBAT 事件需要的怪物数据
    List<Long> combatTemplateIds =
        pool.stream()
            .filter(e -> "COMBAT".equals(e.getEventType()))
            .map(e -> ((Number) e.getParams().get("monster_template_id")).longValue())
            .distinct()
            .toList();
    Map<Long, MonsterTemplate> templateMap =
        combatTemplateIds.isEmpty()
            ? Map.of()
            : monsterTemplateRepository.findByIds(combatTemplateIds).stream()
                .collect(Collectors.toMap(MonsterTemplate::getId, t -> t));
    Set<Long> skillIds =
        templateMap.values().stream()
            .flatMap(
                t -> t.getSkills() != null ? t.getSkills().stream() : java.util.stream.Stream.of())
            .collect(Collectors.toSet());
    Map<Long, Skill> skillMap =
        skillIds.isEmpty()
            ? Map.of()
            : skillRepository.findByIds(new ArrayList<>(skillIds)).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

    Map<String, Object> context = Map.of("mapNode", mapNode, "mapName", mapNode.getName());

    for (int i = 0; i < params.slots(); i++) {
      if (ThreadLocalRandom.current().nextDouble() >= params.perRollChance()) continue;

      ActivityEvent event =
          WeightedRandom.select(pool, ActivityEvent::getWeight, ThreadLocalRandom.current());
      if (event == null) continue;

      if ("COMBAT".equals(event.getEventType())) {
        EncounterResult result =
            combatEventHandler.handle(event, userId, user, templateMap, skillMap, i);
        combatSummary = combatSummary.merge(result);
        if (user.getStatus() == UserStatus.DYING) break;
      } else {
        trainingCompleter.handleNumericEvent(userId, user, event, context);
      }
    }
    return combatSummary;
  }

  private String beautifyTrainingSummary(
      MapNode mapNode,
      long minutesTraining,
      long totalExp,
      List<String> itemNames,
      CombatSummary combatSummary,
      String fallback) {
    String combatHighlight = buildHighlightBattleText(combatSummary);
    var request =
        new ExplorationDescriptionFunction.Request(
            mapNode.getName(),
            mapNode.getDescription(),
            "历时" + minutesTraining + "分钟的野外历练",
            itemNames,
            totalExp > 0 ? totalExp : null,
            null,
            buildCombatSummaryText(combatSummary),
            combatHighlight);
    try {
      var response = explorationDescriptionFunction.beautify(request);
      if (response != null && response.description() != null && !response.description().isEmpty()) {
        return response.description();
      }
    } catch (Exception e) {
      log.warn("LLM 美化历练描述失败", e);
    }
    return fallback;
  }

  private String buildCombatSummaryText(CombatSummary cs) {
    if (cs.totalEncounters() == 0) return "";
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("遇敌%d场 | 击杀%d只", cs.totalEncounters(), cs.totalKills()));
    if (cs.defeatCount() > 0) sb.append(String.format(" | 战败%d场", cs.defeatCount()));
    if (cs.expGained() > 0) sb.append(String.format(" | 修为+%d", cs.expGained()));
    return sb.toString();
  }

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
        sb.append("，HP ")
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
      List<Map<String, Object>> trainingItems,
      boolean diedInTraining) {
    StringBuilder summary = new StringBuilder();
    summary.append(String.format("历练时长: %d 分钟\n", minutesTraining));
    if (totalExp > 0) summary.append(String.format("修为: +%d\n", totalExp));
    if (combatSummary.totalEncounters() > 0) {
      summary.append(buildCombatSummaryText(combatSummary)).append("\n");
    }
    if (!trainingItems.isEmpty()) {
      summary.append("物品:\n");
      for (Map<String, Object> item : trainingItems) {
        summary.append(
            String.format(
                "  %s x%d\n",
                item.get("name"), ((Number) item.getOrDefault("quantity", 1)).intValue()));
      }
    }
    if (diedInTraining) summary.append("\n重伤濒死！30 分钟后自动恢复至 20% HP");
    return summary.toString();
  }

  private double calculateEfficiencyMultiplier(int agility) {
    return 1.0 + Math.min(agility * AGILITY_EFFICIENCY_COEFFICIENT, MAX_EFFICIENCY_BOOST);
  }

  private double calculateLevelDecayMultiplier(int playerLevel, int mapLevel) {
    int levelDiff = playerLevel - mapLevel - LEVEL_DECAY_OFFSET;
    if (levelDiff <= 0) return 1.0;
    return Math.max(MIN_LEVEL_DECAY_MULTIPLIER, 1.0 - levelDiff * LEVEL_DECAY_RATE);
  }

  private List<Map<String, Object>> calculateItemsReward(
      long minutesTraining, double efficiencyMultiplier, MapNode mapNode) {
    var specialties = mapNode.getSpecialties();
    if (specialties == null || specialties.isEmpty()) return List.of();
    Map<Long, ItemTemplate> templateMap =
        itemTemplateRepository
            .findByIds(specialties.stream().map(SpecialtyEntry::templateId).toList())
            .stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    int dropChances = Math.max(1, (int) (minutesTraining / 10.0 * efficiencyMultiplier));
    Map<Long, Map<String, Object>> merged = new LinkedHashMap<>();
    for (int i = 0; i < dropChances; i++) {
      SpecialtyEntry selectedEntry =
          WeightedRandom.select(specialties, SpecialtyEntry::weight, ThreadLocalRandom.current());
      if (selectedEntry == null) continue;
      Long selectedTemplateId = selectedEntry.templateId();
      if (selectedTemplateId == null) continue;
      int quantity = ThreadLocalRandom.current().nextInt(3) + 1;
      Map<String, Object> existing = merged.get(selectedTemplateId);
      if (existing != null) {
        existing.put("quantity", (Integer) existing.get("quantity") + quantity);
      } else {
        ItemTemplate template = templateMap.get(selectedTemplateId);
        String name = template != null ? template.getName() : "未知物品";
        Map<String, Object> item = new HashMap<>();
        item.put("templateId", selectedTemplateId);
        item.put("name", name);
        item.put("quantity", quantity);
        merged.put(selectedTemplateId, item);
      }
    }
    return new ArrayList<>(merged.values());
  }

  private void addTrainingItemsToInventory(Long userId, List<Map<String, Object>> items) {
    if (items == null || items.isEmpty()) return;
    for (Map<String, Object> item : items) {
      Long templateId = TypeUtils.toLong(item.get("templateId"));
      if (templateId == null) continue;
      String name = (String) item.get("name");
      int quantity = ((Number) item.getOrDefault("quantity", 1)).intValue();
      ItemType itemType =
          itemTemplateRepository
              .findById(templateId)
              .map(ItemTemplate::getType)
              .orElse(ItemType.MATERIAL);
      stackableItemService.addStackableItem(userId, templateId, itemType, name, quantity);
    }
  }
}
