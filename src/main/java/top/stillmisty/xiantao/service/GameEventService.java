package top.stillmisty.xiantao.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.repository.GameEventRepository;

/** 游戏事件服务 — 保存事件、查询未投递、标记已投递 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameEventService {

  private final GameEventRepository gameEventRepository;

  /** 保存单个事件 */
  @Transactional
  public GameEvent save(GameEvent event) {
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
