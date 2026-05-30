package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.event.entity.table.GameEventTableDef.GAME_EVENT;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.infrastructure.mapper.GameEventMapper;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

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
        QueryWrapper.create()
            .where(GAME_EVENT.USER_ID.eq(userId))
            .and(GAME_EVENT.DELIVERED.eq(false))
            .orderBy(GAME_EVENT.OCCURRED_AT.asc());
    return gameEventMapper.selectListByQuery(query);
  }

  public int markDelivered(List<Long> eventIds) {
    if (eventIds == null || eventIds.isEmpty()) return 0;
    return gameEventMapper.markDeliveredByIds(eventIds);
  }

  public int deleteDeliveredBefore(int retentionDays) {
    return gameEventMapper.deleteByQuery(
        QueryWrapper.create()
            .where(GAME_EVENT.DELIVERED.eq(true))
            .and(GAME_EVENT.OCCURRED_AT.le(TimeUtil.now().minusDays(retentionDays))));
  }
}
