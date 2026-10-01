package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.worldevent.WorldEventEnvironmentalApplier;

/** 历练完成器 — 产出完成叙事、代理子事件/隐藏事件执行 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingCompleter {

  private final GameEventService gameEventService;
  private final ActivitySubEventPipeline subEventPipeline;
  private final WorldEventEnvironmentalApplier worldEventEnvApplier;

  @Transactional
  public void produceCompletionEvent(
      Long userId,
      Player user,
      MapNode mapNode,
      long minutesTraining,
      long expGained,
      int itemCount) {
    StringBuilder narrative = new StringBuilder("你在{{mapName}}历练了 {{minutes}} 分钟");
    if (expGained > 0 || itemCount > 0) {
      narrative.append("，");
      if (expGained > 0) {
        narrative.append("获得 +{{exp}} 修为");
      }
      if (expGained > 0 && itemCount > 0) {
        narrative.append("、");
      }
      if (itemCount > 0) {
        narrative.append("带回 {{items}} 件物品");
      }
      narrative.append("。");
    } else {
      narrative.append("，本次未遭遇战斗，无收益。");
    }
    Map<String, Object> args =
        Map.of(
            "mapName", mapNode.getName(),
            "minutes", minutesTraining,
            "exp", expGained,
            "items", itemCount);
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAINING_COMPLETE)
            .withNarrative(narrative.toString(), args));
  }

  @Transactional
  public void produceInterruptedEvent(Long userId, MapNode mapNode) {
    Map<String, Object> args = Map.of("mapName", mapNode.getName());
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAINING_INTERRUPTED)
            .withNarrative("你在{{mapName}}的历练因重伤而中断。", args));
  }

  /** 处理单个非 COMBAT 事件（由统一循环调用） */
  @Transactional
  public void handleNumericEvent(
      Long userId, Player user, ActivityEvent event, EventContext context) {
    subEventPipeline.processEventWithContext(
        event, userId, user, GameEventCategory.TRAINING_EVENT, context);
  }

  /** 检查历练隐藏事件 */
  @Transactional
  public void checkHiddenEvents(Long userId, Player user, MapNode mapNode) {
    subEventPipeline.checkHiddenEvents(
        ActivityType.TRAINING.getCode(),
        mapNode.getId(),
        userId,
        user,
        GameEventCategory.TRAINING_HIDDEN,
        fortune -> EventContext.withMapAndFortune(mapNode, fortune));
  }

  /** 应用环境世界事件（历练结算时查询当前地图的区域 + 全局 ENVIRONMENTAL 事件） */
  @Transactional
  public void applyEnvironmentalEvents(Long userId, Player user, MapNode mapNode) {
    worldEventEnvApplier.apply(userId, user, mapNode);
  }
}
