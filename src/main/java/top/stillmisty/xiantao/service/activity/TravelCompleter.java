package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.entity.HiddenCompletion;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.worldevent.WorldEventEnvironmentalApplier;

/** 旅行完成器 — 旅行到达时的子事件和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TravelCompleter {

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final SubEventEffectExecutor effectExecutor;
  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final TriggerConditionChecker triggerConditionChecker;
  private final ActivityEventHelper activityEventHelper;
  private final FortuneService fortuneService;
  private final WorldEventEnvironmentalApplier worldEventEnvApplier;

  @Transactional
  public void completeTravel(Long userId, User user, MapNode fromMap, MapNode toMap) {
    Map<String, Object> arrivalArgs =
        Map.of(
            "from",
            fromMap.getName(),
            "to",
            toMap.getName(),
            "mapName",
            toMap.getName(),
            "mapDescription",
            toMap.getDescription() != null ? toMap.getDescription() : "");
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAVEL_ARRIVED)
            .withNarrative("你经过一路跋涉，终于抵达了{{to}}。", arrivalArgs));

    rollSubEvents(userId, user, toMap);
    checkHiddenEvents(userId, user, toMap);
    applyEnvironmentalEvents(userId, user, toMap);
  }

  private void applyEnvironmentalEvents(Long userId, User user, MapNode mapNode) {
    worldEventEnvApplier.apply(userId, user, mapNode);
  }

  // 旅行事件设计为"低频高风险高回报"：
  // 基础触发概率 0.30，经命运修正后 0.21~0.39。不触发时无事发生。
  // 触发后效果比历练事件大得多（ADD_EXP_PERCENT 代替 ADD_EXP、TAKE_DAMAGE_PERCENT 代替 FLAT）。
  // 每个 ActivityEvent 的 params.effects 必须覆盖 EventType.description 中所有 {{key}} 模板变量，
  // 遗漏会导致 NotificationAppender 渲染出 ？。
  private void rollSubEvents(Long userId, User user, MapNode mapNode) {
    ActivityEvent selected =
        subEventSelector.selectSubEvent(
            ActivityType.TRAVEL.getCode(), mapNode.getId(), 0.30, userId);
    if (selected == null) return;

    var fortune = fortuneService.calculate(userId);
    Map<String, Object> context = new HashMap<>();
    EventContextKeys.MAP_NODE.put(context, mapNode);
    EventContextKeys.MAP_NAME.put(context, mapNode.getName());
    EventContextKeys.FORTUNE.put(context, fortune);
    Map<String, Object> templateArgs = effectExecutor.execute(selected, userId, user, context);
    String narrativeKey = activityEventHelper.resolveNarrativeKey(selected.getCode());
    gameEventService.save(
        GameEvent.create(userId, GameEventCategory.TRAVEL_EVENT)
            .withNarrative(narrativeKey, templateArgs));
  }

  private void checkHiddenEvents(Long userId, User user, MapNode mapNode) {
    var fortune = fortuneService.calculate(userId);
    var hiddenEvents =
        subEventSelector.findHiddenEvents(ActivityType.TRAVEL.getCode(), mapNode.getId());
    for (ActivityEvent event : hiddenEvents) {
      if (!activityEventHelper.checkPrerequisite(userId, event)) continue;
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ActivityType.TRAVEL.getCode(), mapNode.getId(), event.getCode());
      if (alreadyDone) continue;
      if (!triggerConditionChecker.check(event, userId, user)) continue;

      hiddenCompletionRepository.save(
          HiddenCompletion.create(
              userId, ActivityType.TRAVEL.getCode(), mapNode.getId(), event.getCode()));

      Map<String, Object> hiddenContext = new HashMap<>();
      EventContextKeys.MAP_NODE.put(hiddenContext, mapNode);
      EventContextKeys.MAP_NAME.put(hiddenContext, mapNode.getName());
      EventContextKeys.FORTUNE.put(hiddenContext, fortune);
      Map<String, Object> templateArgs = effectExecutor.execute(event, userId, user, hiddenContext);
      String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
      gameEventService.save(
          GameEvent.create(userId, GameEventCategory.TRAVEL_HIDDEN)
              .withNarrative(narrativeKey, templateArgs));
    }
  }
}
