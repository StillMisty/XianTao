package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.infrastructure.mapper.GameEventMapper;

@Repository
@RequiredArgsConstructor
public class GameEventRepository {

  private final GameEventMapper gameEventMapper;

  public GameEvent save(GameEvent event) {
    gameEventMapper.insertOrUpdateSelective(event);
    return event;
  }

  public List<GameEvent> saveAll(List<GameEvent> events) {
    if (events == null || events.isEmpty()) return List.of();
    gameEventMapper.insertBatch(events);
    return events;
  }

  public List<GameEvent> findUndeliveredByUserId(Long userId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(GameEvent::getUserId, userId)
            .eq(GameEvent::getDelivered, false)
            .orderBy(GameEvent::getOccurredAt, true);
    return gameEventMapper.selectListByQuery(query);
  }

  public int markDelivered(List<Long> eventIds) {
    if (eventIds == null || eventIds.isEmpty()) return 0;
    return gameEventMapper.markDeliveredByIds(eventIds);
  }

  public int deleteDeliveredBefore(int retentionDays) {
    return gameEventMapper.deleteByQuery(
        new QueryWrapper()
            .eq(GameEvent::getDelivered, true)
            .le(GameEvent::getOccurredAt, java.time.LocalDateTime.now().minusDays(retentionDays)));
  }
}
