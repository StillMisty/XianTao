package top.stillmisty.xiantao.domain.worldevent.vo;

import java.time.LocalDateTime;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;

public record WorldEventVO(
    Long id,
    String category,
    String scope,
    String title,
    String description,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    boolean participationEnabled,
    int participationLimit,
    int participationCount) {

  public static WorldEventVO from(WorldEvent event) {
    return new WorldEventVO(
        event.getId(),
        event.getCategory().getName(),
        event.getScope().getName(),
        event.getTitle(),
        event.getDescription(),
        event.getStatus().getName(),
        event.getStartTime(),
        event.getEndTime(),
        Boolean.TRUE.equals(event.getParticipationEnabled()),
        event.getParticipationLimit() != null ? event.getParticipationLimit() : 0,
        event.getParticipationCount());
  }
}
