package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.ActivityEvent;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.HiddenCompletionRepository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.GameEventService;

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
        subEventSelector.selectSubEvent(ActivityType.TRAVEL.getCode(), mapNode.getId(), 1.0);
    if (selected == null) return;

    Map<String, Object> context = Map.of("mapNode", mapNode, "mapName", mapNode.getName());
    Map<String, Object> templateArgs = effectExecutor.execute(selected, userId, user, context);
    gameEventService.createEvent(
        userId, GameEventCategory.TRAVEL_EVENT, selected.getCode(), templateArgs);
  }

  private void checkHiddenEvents(Long userId, User user, MapNode mapNode) {
    var hiddenEvents =
        subEventSelector.findHiddenEvents(ActivityType.TRAVEL.getCode(), mapNode.getId());
    for (ActivityEvent event : hiddenEvents) {
      boolean alreadyDone =
          hiddenCompletionRepository.exists(
              userId, ActivityType.TRAVEL.getCode(), mapNode.getId(), event.getCode());
      if (alreadyDone) continue;
      if (!triggerConditionChecker.check(event, userId, user)) continue;

      hiddenCompletionRepository.save(
          top.stillmisty.xiantao.domain.event.entity.HiddenCompletion.create(
              userId, ActivityType.TRAVEL.getCode(), mapNode.getId(), event.getCode()));

      Map<String, Object> templateArgs =
          effectExecutor.execute(
              event, userId, user, Map.of("mapNode", mapNode, "mapName", mapNode.getName()));
      gameEventService.createEvent(
          userId,
          GameEventCategory.TRAVEL_HIDDEN,
          "你在{{mapName}}发现了隐藏的秘密：{{eventName}}。",
          Map.of("mapName", mapNode.getName(), "eventName", event.getCode()));
    }
  }
}
