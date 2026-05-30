package top.stillmisty.xiantao.service.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.AreaConfig;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate.Poi;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonSpiritStateRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonTemplateRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.ai.ToolExecutor;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("NullAway")
public class DungeonTools {

  private final ToolExecutor toolExecutor;
  private final DungeonTemplateRepository dungeonTemplateRepository;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonSpiritStateRepository spiritStateRepository;
  private final DungeonStateBuilder stateBuilder;
  private final DungeonCombatHelper combatHelper;
  private final DungeonLootHelper lootHelper;
  private final DungeonSpiritStateHelper spiritStateHelper;
  private final UserStateService userStateService;
  private final SpiritStoneService spiritStoneService;
  private final DungeonProgressHelper progressHelper;

  /**
   * 探索当前区域的指定 POI。根据 POI 类型执行战斗或采集。
   *
   * <p>poi_name 必须是当前区域 mainPois 或 hiddenPois 中的名称。 每次探索消耗该 POI（不可重复探索）。
   *
   * @param poiName POI 名称（精确匹配）
   * @param approach 探索方式（可选，如"仔细搜索"、"直接战斗"、"绕路"等），用于隐藏 POI 发现判断
   */
  @Tool(description = "探索当前区域的指定地点。poiName 是地点名称（精确匹配），approach 是探索方式（可选）")
  @Transactional
  public ResolveEncounterResponse resolveEncounter(
      @ToolParam(description = "地点名称（精确匹配）") String poiName,
      @ToolParam(description = "探索方式描述", required = false) String approach) {

    return toolExecutor.execute(
        "resolveEncounter",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          User user = userStateService.loadUser(userId);
          DungeonInstance instance = findActiveInstance(userId);
          DungeonTemplate dungeon =
              dungeonTemplateRepository
                  .findById(instance.getDungeonId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));
          DungeonSpiritState spiritState = findSpiritState(instance.getId(), userId);

          AreaConfig area = stateBuilder.findArea(dungeon, instance.getCurrentAreaKey());
          if (area == null) {
            return ResolveEncounterResponse.error("当前区域不存在");
          }

          Poi poi = stateBuilder.findPoi(area, poiName);
          if (poi == null) {
            return ResolveEncounterResponse.error(
                "找不到地点「"
                    + poiName
                    + "」。当前区域可探索的地点："
                    + String.join(", ", area.allPois().stream().map(Poi::name).toList()));
          }

          if (instance.hasExploredPoi(poi.name())) {
            return ResolveEncounterResponse.error("地点「" + poiName + "」已经探索过了");
          }

          if (poi.isPassage()) {
            return ResolveEncounterResponse.error("通道地点不需要主动探索，全清当前区域主线 POI 后自动激活");
          }

          boolean isHiddenPoi =
              area.hiddenPois() != null
                  && area.hiddenPois().stream().anyMatch(p -> p.name().equals(poi.name()));

          List<String> hiddenFinds = new ArrayList<>();
          if (isHiddenPoi && spiritState != null) {
            spiritState.addHiddenFind(poi.name());
            spiritStateRepository.save(spiritState);
            hiddenFinds.add(poi.name());
          }

          instance.addExploredPoi(poi.name());

          String combatSummary = null;
          boolean playerWon = true;
          long expGained = 0;
          List<String> lootDesc = new ArrayList<>();
          long spiritStones = 0;

          if (poi.isCombat()) {
            DungeonTemplate.MonsterEntry monsterEntry = combatHelper.selectMonster(poi);
            if (monsterEntry == null) {
              instanceRepository.save(instance);
              return ResolveEncounterResponse.error("怪物池为空");
            }

            DungeonCombatHelper.SimpleCombatOutcome outcome =
                combatHelper.executeCombat(userId, user, poi, monsterEntry);

            playerWon = outcome.playerWon();
            combatSummary = outcome.summary();
            expGained = outcome.expGained();

            if (!playerWon) {
              combatSummary = outcome.summary();
              instanceRepository.save(instance);
              return new ResolveEncounterResponse(
                  "COMBAT",
                  poiName,
                  false,
                  "战斗失败",
                  combatSummary,
                  0,
                  List.of(),
                  0,
                  hiddenFinds,
                  false);
            }

            var loot = lootHelper.rollAndGiveLoot(userId, poi);
            lootDesc = loot.descriptions();
            spiritStones = loot.spiritStones();

          } else if (poi.isGather() || poi.isSearch()) {
            boolean triggerCombat =
                poi.isSearch() && ThreadLocalRandom.current().nextDouble() < 0.2;

            if (triggerCombat && poi.hasMonsterPool()) {
              DungeonTemplate.MonsterEntry monsterEntry = combatHelper.selectMonster(poi);
              if (monsterEntry != null) {
                DungeonCombatHelper.SimpleCombatOutcome outcome =
                    combatHelper.executeCombat(userId, user, poi, monsterEntry);
                playerWon = outcome.playerWon();
                combatSummary = "搜索时遭遇了隐藏的怪物！" + outcome.summary();
                expGained = outcome.expGained();
                if (!playerWon) {
                  instanceRepository.save(instance);
                  return new ResolveEncounterResponse(
                      poi.type(),
                      poiName,
                      false,
                      "搜索遭遇战斗失败",
                      combatSummary,
                      0,
                      List.of(),
                      0,
                      hiddenFinds,
                      false);
                }
              }
            }

            var loot = lootHelper.rollAndGiveLoot(userId, poi);
            lootDesc = loot.descriptions();
            spiritStones = loot.spiritStones();

            if (!triggerCombat && poi.isSearch()) {
              combatSummary = "仔细搜索了一番，发现了有用的物资。";
            }
          }

          boolean allMainResolved =
              area.mainPois().stream()
                  .filter(p -> !p.isPassage())
                  .allMatch(p -> instance.hasExploredPoi(p.name()));

          if (allMainResolved) {
            instance.setPassageUnlocked(true);
          }

          instanceRepository.save(instance);

          boolean passageUnlocked = instance.getPassageUnlocked();

          return new ResolveEncounterResponse(
              poi.type(),
              poiName,
              playerWon,
              poi.description(),
              combatSummary,
              expGained,
              lootDesc,
              spiritStones,
              hiddenFinds,
              passageUnlocked);
        });
  }

  /** 推进到下一区域（仅在通道解锁后可用） */
  @Tool(description = "推进到下一区域。仅在当前区域所有主线 POI 已探索、通道已解锁后调用")
  @Transactional
  public AdvanceAreaResponse advanceToNextArea() {
    return toolExecutor.execute(
        "advanceToNextArea",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          User user = userStateService.loadUser(userId);
          DungeonInstance instance = findActiveInstance(userId);
          DungeonTemplate dungeon =
              dungeonTemplateRepository
                  .findById(instance.getDungeonId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));

          if (!instance.getPassageUnlocked()) {
            return new AdvanceAreaResponse(false, null, null, "通道尚未解锁，请先探索完当前区域的所有主线地点");
          }

          AreaConfig nextArea = stateBuilder.findNextArea(dungeon, instance.getCurrentAreaKey());
          if (nextArea == null) {
            progressHelper.completeDungeon(userId, instance);
            return new AdvanceAreaResponse(true, null, null, "你已经通关了秘境！");
          }

          instance.advanceArea(nextArea.key());
          instanceRepository.save(instance);

          DungeonSpiritState spiritState = findSpiritState(instance.getId(), userId);
          String favorAttitude = spiritState != null ? spiritState.favorAttitude() : null;

          List<String> newPoiNames = nextArea.mainPois().stream().map(Poi::name).toList();

          return new AdvanceAreaResponse(
              true,
              nextArea.name(),
              nextArea.description(),
              "进入了【"
                  + nextArea.name()
                  + "】。"
                  + nextArea.description()
                  + "\n可探索地点："
                  + String.join(", ", newPoiNames));
        });
  }

  /** 撤退并结算 */
  @Tool(description = "撤退离开秘境。保留已获得的奖励")
  @Transactional
  public RetreatResponse retreatFromDungeon() {
    return toolExecutor.execute(
        "retreatFromDungeon",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          User user = userStateService.loadUser(userId);
          DungeonInstance instance = findActiveInstance(userId);

          instance.markAbandoned();
          instanceRepository.save(instance);

          user.clearActivity();
          userStateService.saveActivity(user);

          return new RetreatResponse(true, "你已经离开了秘境，已获奖励保留。");
        });
  }

  /** 查看自身状态 */
  @Tool(description = "查看探索者的自身属性、好感度等信息")
  public PlayerStatusResponse checkPlayerStatus() {
    return toolExecutor.execute(
        "checkPlayerStatus",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          User user = userStateService.loadUser(userId);
          DungeonInstance instance = findActiveInstance(userId);
          DungeonSpiritState spiritState = findSpiritState(instance.getId(), userId);

          return new PlayerStatusResponse(
              user.getNickname(),
              user.getLevel() != null ? user.getLevel() : 0,
              user.getHpCurrent() != null ? user.getHpCurrent() : 0,
              spiritState != null ? spiritState.getFavor() : 0,
              spiritState != null ? spiritState.favorAttitude() : "未知",
              spiritStoneService.getBalance(userId));
        });
  }

  /** 查看当前区域状态 */
  @Tool(description = "查看当前区域已探索和剩余的 POI")
  public CurrentAreaResponse checkCurrentArea() {
    return toolExecutor.execute(
        "checkCurrentArea",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          DungeonInstance instance = findActiveInstance(userId);
          DungeonTemplate dungeon =
              dungeonTemplateRepository
                  .findById(instance.getDungeonId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));

          AreaConfig area = stateBuilder.findArea(dungeon, instance.getCurrentAreaKey());
          if (area == null) {
            return new CurrentAreaResponse(
                instance.getCurrentAreaKey(), List.of(), List.of(), false);
          }

          List<String> explored =
              instance.getExploredPois() != null
                  ? instance.getExploredPois().stream()
                      .map(DungeonInstance.ExploredPoiRecord::poiName)
                      .toList()
                  : List.of();

          List<String> remaining =
              area.mainPois().stream()
                  .filter(p -> !p.isPassage())
                  .map(Poi::name)
                  .filter(n -> !explored.contains(n))
                  .toList();

          return new CurrentAreaResponse(
              area.name(), explored, remaining, instance.getPassageUnlocked());
        });
  }

  /** 调整好感度（仅好感系统开启时可用） */
  @Tool(description = "根据探索者的言行调整好感度。change 数值可正可负，reason 是变更原因简述。谨慎使用")
  @Transactional
  public AdjustFavorResponse adjustFavor(
      @ToolParam(description = "好感度变更量（正数增加，负数减少）") int change,
      @ToolParam(description = "变更原因简述") String reason) {

    return toolExecutor.execute(
        "adjustFavor",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          DungeonInstance instance = findActiveInstance(userId);
          DungeonTemplate dungeon =
              dungeonTemplateRepository
                  .findById(instance.getDungeonId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));

          if (!dungeon.hasAffectionSystem()) {
            return new AdjustFavorResponse(0, "无好感系统", "此秘境无好感系统");
          }

          DungeonSpiritState spiritState = findOrCreateSpiritState(instance, userId);
          spiritState.addFavor(change, reason);
          spiritStateRepository.save(spiritState);

          return new AdjustFavorResponse(
              spiritState.getFavor(), spiritState.favorAttitude(), "好感度已调整");
        });
  }

  /** 消耗好感度获取隐藏线索 */
  @SuppressWarnings("NullAway")
  @Tool(description = "消耗好感度向秘境之灵询问隐藏线索。消耗 20~30 好感度。仅好感系统开启时可用")
  @Transactional
  public GiveHintResponse giveHint() {
    return toolExecutor.execute(
        "giveHint",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          DungeonInstance instance = findActiveInstance(userId);
          DungeonTemplate dungeon =
              dungeonTemplateRepository
                  .findById(instance.getDungeonId())
                  .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NOT_FOUND, ""));
          DungeonSpiritState spiritState = findSpiritState(instance.getId(), userId);

          if (!dungeon.hasAffectionSystem()) {
            return new GiveHintResponse("", false, "此秘境无秘境之灵");
          }

          if (spiritState == null
              || spiritState.getFavor() == null
              || spiritState.getFavor() < 20) {
            return new GiveHintResponse("", false, "好感度不足（需要至少20）");
          }

          AreaConfig area = stateBuilder.findArea(dungeon, instance.getCurrentAreaKey());
          if (area == null || area.hiddenPois() == null || area.hiddenPois().isEmpty()) {
            return new GiveHintResponse("", false, "当前区域无可揭示的隐藏内容");
          }

          List<Poi> undiscovered =
              area.hiddenPois().stream().filter(p -> !instance.hasExploredPoi(p.name())).toList();

          if (undiscovered.isEmpty()) {
            return new GiveHintResponse("", false, "所有隐藏地点已发现");
          }

          int cost = 20 + ThreadLocalRandom.current().nextInt(11);
          spiritState.addFavor(-cost, "消耗好感度获取线索");
          spiritStateRepository.save(spiritState);

          Poi hintPoi = undiscovered.get(ThreadLocalRandom.current().nextInt(undiscovered.size()));
          String hint =
              hintPoi.hasClues()
                  ? hintPoi.clues().get(ThreadLocalRandom.current().nextInt(hintPoi.clues().size()))
                  : "似乎有一处不同寻常之处等待探索...";

          return new GiveHintResponse(
              hint, true, "消耗了" + cost + "好感度，获得了一条线索（剩余好感度：" + spiritState.getFavor() + "）");
        });
  }

  private DungeonInstance findActiveInstance(Long userId) {
    User user = userStateService.loadUser(userId);
    if (user.getActivityTargetId() == null) {
      throw new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE);
    }
    return instanceRepository
        .findByIdForUpdate(user.getActivityTargetId())
        .filter(DungeonInstance::isActive)
        .orElseThrow(() -> new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE));
  }

  private DungeonSpiritState findSpiritState(Long instanceId, Long userId) {
    return spiritStateRepository.findByInstanceIdAndUserId(instanceId, userId).orElse(null);
  }

  private DungeonSpiritState findOrCreateSpiritState(DungeonInstance instance, Long userId) {
    return spiritStateHelper.findOrCreate(instance.getId(), instance.getDungeonId(), userId);
  }

  public record ResolveEncounterResponse(
      String poiType,
      String poiName,
      boolean success,
      String poiDescription,
      String combatSummary,
      long expGained,
      List<String> lootDescriptions,
      long spiritStones,
      List<String> hiddenFinds,
      boolean passageUnlocked) {
    public static ResolveEncounterResponse error(String message) {
      return new ResolveEncounterResponse(
          "ERROR", "", false, message, null, 0, List.of(), 0, List.of(), false);
    }
  }

  public record AdvanceAreaResponse(
      boolean success, String newAreaName, String newAreaDescription, String message) {}

  public record RetreatResponse(boolean success, String message) {}

  public record PlayerStatusResponse(
      String nickname,
      int level,
      int hpCurrent,
      int favor,
      String favorAttitude,
      long spiritStoneBalance) {}

  public record CurrentAreaResponse(
      String areaName,
      List<String> exploredPois,
      List<String> remainingPois,
      boolean passageUnlocked) {}

  public record AdjustFavorResponse(int currentFavor, String attitude, String message) {}

  public record GiveHintResponse(String hint, boolean success, String message) {}
}
