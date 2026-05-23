package top.stillmisty.xiantao.service.worldevent;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.service.GameEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorldEventEnvironmentalApplier {

  private final WorldEventService worldEventService;
  private final WorldEventEffectApplier worldEventEffectApplier;
  private final GameEventService gameEventService;

  @Transactional
  public void apply(Long userId, User user, MapNode mapNode) {
    List<WorldEvent> regionalEvents = worldEventService.findActiveByRegion(mapNode.getId());
    List<WorldEvent> globalEvents = worldEventService.findActiveGlobalEvents();

    List<WorldEvent> envEvents =
        regionalEvents.stream()
            .filter(e -> e.getCategory() == WorldEventCategory.ENVIRONMENTAL)
            .toList();
    if (envEvents.isEmpty()) {
      envEvents =
          globalEvents.stream()
              .filter(e -> e.getCategory() == WorldEventCategory.ENVIRONMENTAL)
              .toList();
    }

    for (WorldEvent event : envEvents) {
      if (!event.hasEffects()) continue;
      Map<String, Object> result = worldEventEffectApplier.applyEffects(event, user);
      if (result.isEmpty()) continue;

      gameEventService.save(
          GameEvent.create(userId, GameEventCategory.WORLD_EVENT)
              .withNarrative("【" + event.getTitle() + "】" + event.getDescription(), result));
    }
  }
}
