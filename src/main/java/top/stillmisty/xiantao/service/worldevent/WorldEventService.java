package top.stillmisty.xiantao.service.worldevent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventStatus;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldEventService {

  private final WorldEventRepository worldEventRepository;

  @Transactional(readOnly = true)
  public Optional<WorldEvent> findById(Long id) {
    return worldEventRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public List<WorldEvent> findActiveEvents() {
    return worldEventRepository.findActiveEvents();
  }

  @Transactional(readOnly = true)
  public List<WorldEvent> findActiveByRegion(Long mapNodeId) {
    return worldEventRepository.findActiveByRegion(mapNodeId);
  }

  @Transactional(readOnly = true)
  public List<WorldEvent> findActiveGlobalEvents() {
    return worldEventRepository.findActiveByScope(WorldEventScope.GLOBAL);
  }

  @Transactional(readOnly = true)
  public List<WorldEvent> findActiveEventsByCategory(WorldEventCategory category) {
    return worldEventRepository.findActiveEvents().stream()
        .filter(e -> e.getCategory() == category)
        .toList();
  }

  @Transactional
  public WorldEvent create(WorldEvent event) {
    if (event.getStatus() == null) {
      event.setStatus(WorldEventStatus.ACTIVE);
    }
    event.setCreatedAt(LocalDateTime.now());
    return worldEventRepository.save(event);
  }

  @Transactional
  public void markExpired(Long id) {
    worldEventRepository.markStatus(id, WorldEventStatus.EXPIRED.getCode());
  }

  @Transactional
  public int refreshActiveStatus() {
    int count = 0;
    List<WorldEvent> activeEvents = worldEventRepository.findActiveEvents();
    for (WorldEvent event : activeEvents) {
      if (event.getEndTime() != null && event.getEndTime().isBefore(LocalDateTime.now())) {
        worldEventRepository.markStatus(event.getId(), WorldEventStatus.EXPIRED.getCode());
        count++;
        activateChainedChildren(event);
      }
    }
    List<WorldEvent> upcoming = worldEventRepository.findUpcoming();
    for (WorldEvent event : upcoming) {
      if (event.getStartTime() != null && !event.getStartTime().isAfter(LocalDateTime.now())) {
        worldEventRepository.markStatus(event.getId(), WorldEventStatus.ACTIVE.getCode());
        count++;
      }
    }
    return count;
  }

  /** 事件过期时，激活以此事件为 parent 的子链事件 */
  private void activateChainedChildren(WorldEvent expiredEvent) {
    List<WorldEvent> allUpcoming = worldEventRepository.findUpcoming();
    for (WorldEvent upcoming : allUpcoming) {
      if (upcoming.getParentEventId() != null
          && upcoming.getParentEventId().equals(expiredEvent.getId())) {
        LocalDateTime now = LocalDateTime.now();
        upcoming.setStartTime(now);
        upcoming.setEndTime(now.plusHours(6));
        upcoming.setStatus(WorldEventStatus.ACTIVE);
        worldEventRepository.save(upcoming);
        log.info("事件链触发: {} → {}", expiredEvent.getTitle(), upcoming.getTitle());
      }
    }
  }

  @Transactional(readOnly = true)
  public int countActiveEvents() {
    return worldEventRepository.findActiveEvents().size();
  }

  @Scheduled(fixedRate = 1800000)
  @Transactional
  public void scheduledLifecycle() {
    try {
      int changed = refreshActiveStatus();
      if (changed > 0) {
        log.debug("世界事件状态更新 {} 条", changed);
      }
    } catch (Exception e) {
      log.warn("世界事件定时状态更新失败: {}", e.getMessage());
    }
  }
}
