package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.dungeon.entity.table.DungeonProgressTableDef.DUNGEON_PROGRESS;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonProgressMapper;

@Repository
@RequiredArgsConstructor
public class DungeonProgressRepository {

  private final DungeonProgressMapper mapper;

  public DungeonProgress save(DungeonProgress progress) {
    mapper.insertOrUpdateSelective(progress);
    return progress;
  }

  /** 原子累加对话互动次数 */
  public int incrementInteractionCount(Long userId, Long dungeonId) {
    return mapper.incrementInteractionCount(userId, dungeonId);
  }

  public Optional<DungeonProgress> findByUserIdAndDungeonId(Long userId, Long dungeonId) {
    QueryWrapper qw =
        QueryWrapper.create()
            .where(DUNGEON_PROGRESS.USER_ID.eq(userId))
            .and(DUNGEON_PROGRESS.DUNGEON_ID.eq(dungeonId));
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  /** 带行锁读取，用于通关结算防并发双首通/超额奖励 */
  public Optional<DungeonProgress> findByUserIdAndDungeonIdForUpdate(Long userId, Long dungeonId) {
    QueryWrapper qw =
        QueryWrapper.create()
            .where(DUNGEON_PROGRESS.USER_ID.eq(userId))
            .and(DUNGEON_PROGRESS.DUNGEON_ID.eq(dungeonId))
            .forUpdate();
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  public List<DungeonProgress> findByUserIdAndDungeonIds(Long userId, List<Long> dungeonIds) {
    if (dungeonIds == null || dungeonIds.isEmpty()) return List.of();
    QueryWrapper qw =
        QueryWrapper.create()
            .where(DUNGEON_PROGRESS.USER_ID.eq(userId))
            .and(DUNGEON_PROGRESS.DUNGEON_ID.in(dungeonIds));
    return mapper.selectListByQuery(qw);
  }
}
