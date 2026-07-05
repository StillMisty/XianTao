package top.stillmisty.xiantao.service.ai;

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
import top.stillmisty.xiantao.infrastructure.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.repository.DungeonSpiritStateRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.dungeon.DungeonCombatHelper;
import top.stillmisty.xiantao.service.dungeon.DungeonLootHelper;

/** 秘境探索战斗工具 — 探索 POI、战斗、采集 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("NullAway")
public class DungeonExplorationTools {

  private final ToolExecutor toolExecutor;
  private final DungeonInstanceRepository instanceRepository;
  private final DungeonSpiritStateRepository spiritStateRepository;
  private final DungeonStateBuilder stateBuilder;
  private final DungeonCombatHelper combatHelper;
  private final DungeonLootHelper lootHelper;

  @Tool(description = "探索当前区域的指定地点。poiName 是地点名称（精确匹配），approach 是探索方式（可选）")
  @Transactional
  public ResolveEncounterResponse resolveEncounter(
      @ToolParam(description = "地点名称（精确匹配）") String poiName,
      @ToolParam(description = "探索方式描述", required = false) String approach) {

    return toolExecutor.execute(
        "resolveEncounter",
        () -> {
          DungeonChatContext ctx = requireContext();
          DungeonInstance instance = ctx.instance();
          DungeonTemplate dungeon = ctx.dungeon();
          DungeonSpiritState spiritState = ctx.spiritState();

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
                combatHelper.executeCombat(ctx.user().getId(), ctx.user(), poi, monsterEntry);

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

            var loot = lootHelper.rollAndGiveLoot(ctx.user().getId(), poi);
            lootDesc = loot.descriptions();
            spiritStones = loot.spiritStones();

          } else if (poi.isGather() || poi.isSearch()) {
            boolean triggerCombat =
                poi.isSearch() && ThreadLocalRandom.current().nextDouble() < 0.2;

            if (triggerCombat && poi.hasMonsterPool()) {
              DungeonTemplate.MonsterEntry monsterEntry = combatHelper.selectMonster(poi);
              if (monsterEntry != null) {
                DungeonCombatHelper.SimpleCombatOutcome outcome =
                    combatHelper.executeCombat(ctx.user().getId(), ctx.user(), poi, monsterEntry);
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

            var loot = lootHelper.rollAndGiveLoot(ctx.user().getId(), poi);
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
              instance.getPassageUnlocked());
        });
  }

  private static DungeonChatContext requireContext() {
    DungeonChatContext ctx = DungeonChatContext.current();
    if (ctx == null) {
      throw new BusinessException(ErrorCode.DUNGEON_NO_ACTIVE_INSTANCE);
    }
    return ctx;
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
}
