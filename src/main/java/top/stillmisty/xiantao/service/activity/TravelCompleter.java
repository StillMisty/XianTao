package top.stillmisty.xiantao.service.activity;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;

/** 旅行完成器 — 旅行到达时的子事件和隐藏事件 */
@Slf4j
@Component
public class TravelCompleter {

  private final GameEventService gameEventService;
  private final SubEventSelector subEventSelector;
  private final SubEventEffectExecutor effectExecutor;
  private final HiddenCompletionRepository hiddenCompletionRepository;
  private final TriggerConditionChecker triggerConditionChecker;
  private final ActivityEventHelper activityEventHelper;
  private final FortuneService fortuneService;

  public TravelCompleter(
      GameEventService gameEventService,
      SubEventSelector subEventSelector,
      @Lazy SubEventEffectExecutor effectExecutor,
      HiddenCompletionRepository hiddenCompletionRepository,
      TriggerConditionChecker triggerConditionChecker,
      FortuneService fortuneService,
      ActivityEventHelper activityEventHelper) {
    this.gameEventService = gameEventService;
    this.subEventSelector = subEventSelector;
    this.effectExecutor = effectExecutor;
    this.hiddenCompletionRepository = hiddenCompletionRepository;
    this.triggerConditionChecker = triggerConditionChecker;
    this.fortuneService = fortuneService;
    this.activityEventHelper = activityEventHelper;
  }

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
    gameEventService.createEvent(
        userId, GameEventCategory.TRAVEL_ARRIVED, "你经过一路跋涉，终于抵达了{{to}}。", arrivalArgs);

    rollSubEvents(userId, user, toMap);
    checkHiddenEvents(userId, user, toMap);
  }

  private void rollSubEvents(Long userId, User user, MapNode mapNode) {
    ActivityEvent selected =
        subEventSelector.selectSubEvent(
            ActivityType.TRAVEL.getCode(), mapNode.getId(), 1.0, userId);
    if (selected == null) return;

    var fortune = fortuneService.calculate(userId);
    Map<String, Object> context = new HashMap<>();
    EventContextKeys.MAP_NODE.put(context, mapNode);
    EventContextKeys.MAP_NAME.put(context, mapNode.getName());
    EventContextKeys.FORTUNE.put(context, fortune);
    Map<String, Object> templateArgs = effectExecutor.execute(selected, userId, user, context);
    String narrativeKey = activityEventHelper.resolveNarrativeKey(selected.getCode());
    gameEventService.createEvent(
        userId, GameEventCategory.TRAVEL_EVENT, narrativeKey, templateArgs);
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
          top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
              userId, ActivityType.TRAVEL.getCode(), mapNode.getId(), event.getCode()));

      Map<String, Object> hiddenContext = new HashMap<>();
      EventContextKeys.MAP_NODE.put(hiddenContext, mapNode);
      EventContextKeys.MAP_NAME.put(hiddenContext, mapNode.getName());
      EventContextKeys.FORTUNE.put(hiddenContext, fortune);
      Map<String, Object> templateArgs = effectExecutor.execute(event, userId, user, hiddenContext);
      String narrativeKey = activityEventHelper.resolveNarrativeKey(event.getCode());
      gameEventService.createEvent(
          userId, GameEventCategory.TRAVEL_HIDDEN, narrativeKey, templateArgs);
    }
  }
}
