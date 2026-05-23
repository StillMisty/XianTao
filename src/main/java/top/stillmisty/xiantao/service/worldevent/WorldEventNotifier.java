package top.stillmisty.xiantao.service.worldevent;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.service.GameEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorldEventNotifier {

  private final GameEventService gameEventService;

  /** 为指定用户创建世界事件通知 */
  public void notifyUser(Long userId, WorldEvent event) {
    GameEvent gameEvent =
        GameEvent.create(userId, GameEventCategory.WORLD_EVENT)
            .withNarrative(
                "世界事件",
                java.util.Map.of(
                    "eventTitle", event.getTitle(),
                    "eventDescription", event.getDescription(),
                    "category", event.getCategory().getName()));

    gameEventService.save(gameEvent);
  }

  /** 为多个用户批量创建世界事件通知 */
  public void notifyUsers(List<Long> userIds, WorldEvent event) {
    for (Long userId : userIds) {
      notifyUser(userId, event);
    }
  }
}
