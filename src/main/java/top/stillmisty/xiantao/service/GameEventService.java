package top.stillmisty.xiantao.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.event.repository.GameEventRepository;

/** 游戏事件服务 — 插入事件、查询未投递、标记已投递 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameEventService {

  private final GameEventRepository gameEventRepository;

  /** 创建并保存一个事件 */
  @Transactional
  public GameEvent createEvent(Long userId, GameEventCategory category) {
    GameEvent event = GameEvent.create(userId, category);
    return gameEventRepository.save(event);
  }

  /** 创建带叙事的事件 */
  @Transactional
  public GameEvent createEvent(
      Long userId,
      GameEventCategory category,
      String narrativeKey,
      Map<String, Object> narrativeArgs) {
    GameEvent event = GameEvent.create(userId, category).withNarrative(narrativeKey, narrativeArgs);
    return gameEventRepository.save(event);
  }

  /** 创建带叙事和效果的事件 */
  @Transactional
  public GameEvent createEvent(
      Long userId,
      GameEventCategory category,
      String narrativeKey,
      Map<String, Object> narrativeArgs,
      Map<String, Object> effects) {
    GameEvent event =
        GameEvent.create(userId, category)
            .withNarrative(narrativeKey, narrativeArgs)
            .withEffects(effects);
    return gameEventRepository.save(event);
  }

  /** 批量保存事件 */
  @Transactional
  public List<GameEvent> saveAll(List<GameEvent> events) {
    return gameEventRepository.saveAll(events);
  }

  /** 查询用户所有未投递事件 */
  public List<GameEvent> findUndelivered(Long userId) {
    return gameEventRepository.findUndeliveredByUserId(userId);
  }

  /** 标记事件为已投递 */
  @Transactional
  public int markDelivered(List<Long> eventIds) {
    if (eventIds == null || eventIds.isEmpty()) return 0;
    return gameEventRepository.markDelivered(eventIds);
  }

  /** 清理超过保留天数的已投递事件 */
  @Transactional
  public int cleanupDelivered(int retentionDays) {
    return gameEventRepository.deleteDeliveredBefore(retentionDays);
  }
}
