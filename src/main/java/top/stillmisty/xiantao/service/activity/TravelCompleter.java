package top.stillmisty.xiantao.service.activity;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.notification.entity.GameEvent;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.worldevent.WorldEventEnvironmentalApplier;

/** 旅行完成器 — 旅行到达时的子事件和隐藏事件 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TravelCompleter {

  private final GameEventService gameEventService;
  private final ActivitySubEventPipeline subEventPipeline;
  private final WorldEventEnvironmentalApplier worldEventEnvApplier;

  @Transactional
  public void completeTravel(Long userId, Player user, MapNode fromMap, MapNode toMap) {
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

  private void applyEnvironmentalEvents(Long userId, Player user, MapNode mapNode) {
    worldEventEnvApplier.apply(userId, user, mapNode);
  }

  private void rollSubEvents(Long userId, Player user, MapNode mapNode) {
    subEventPipeline.rollSubEvent(
        ActivityType.TRAVEL.getCode(),
        mapNode.getId(),
        0.30,
        userId,
        user,
        GameEventCategory.TRAVEL_EVENT,
        fortune -> EventContext.withMapAndFortune(mapNode, fortune));
  }

  private void checkHiddenEvents(Long userId, Player user, MapNode mapNode) {
    subEventPipeline.checkHiddenEvents(
        ActivityType.TRAVEL.getCode(),
        mapNode.getId(),
        userId,
        user,
        GameEventCategory.TRAVEL_HIDDEN,
        fortune -> EventContext.withMapAndFortune(mapNode, fortune));
  }
}
