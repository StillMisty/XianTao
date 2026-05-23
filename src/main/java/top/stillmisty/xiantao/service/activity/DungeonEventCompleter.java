package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.GameEventService;

/**
 * 秘境事件完成器 — 在探索POI、推进区域、通关时产出 DUNGEON 事件。
 *
 * <p>探索事件使用 SubEventSelector 从 xt_activity_event 池中加权随机选取，类似旅行/历练模式。 推进区域事件和通关事件为固定叙事 + 直接
 * GameEvent 写入。
 *
 * <p>每个 ActivityEvent.params.effects 必须覆盖 EventType.description 中所有 {{key}} 模板变量。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DungeonEventCompleter {

  private static final double EXPLORE_TRIGGER_CHANCE = 0.20;

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final SubEventEffectExecutor effectExecutor;
  private final ActivityEventHelper activityEventHelper;

  /** 进入新秘境区域（外围→内围→核心），产出固定叙事事件 */
  @Transactional
  public void onAreaAdvance(
      Long userId, User user, Long dungeonId, String dungeonName, String areaName) {
    Map<String, Object> args = Map.of("dungeonName", dungeonName, "area", areaName);
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.DUNGEON_ENTER)
            .withNarrative("踏入{{area}}，眼前的景象与外围截然不同，灵气浓度明显提升。", args));
  }

  /** 探索 POI 后随机触发 DUNGEON 事件（20% 基础概率） */
  @Transactional
  public void rollExploreEvent(Long userId, User user, Long dungeonId) {
    ActivityEvent selected =
        subEventSelector.selectSubEvent(
            ActivityType.DUNGEON.getCode(), dungeonId, EXPLORE_TRIGGER_CHANCE, userId);
    if (selected == null) return;

    Map<String, Object> context = new HashMap<>();
    Map<String, Object> templateArgs = effectExecutor.execute(selected, userId, user, context);
    String narrativeKey = activityEventHelper.resolveNarrativeKey(selected.getCode());
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.DUNGEON_EXPLORE)
            .withNarrative(narrativeKey, templateArgs));
  }

  /** 秘境通关，产出结算叙事事件 */
  @Transactional
  public void onComplete(Long userId, User user, String dungeonName) {
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.DUNGEON_COMPLETE)
            .withNarrative("秘境【{{dungeonName}}】探索圆满完成！", Map.of("dungeonName", dungeonName)));
  }
}
