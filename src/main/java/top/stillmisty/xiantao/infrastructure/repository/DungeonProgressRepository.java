package top.stillmisty.xiantao.infrastructure.repository;

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

  public Optional<DungeonProgress> findByUserIdAndDungeonId(Long userId, Long dungeonId) {
    QueryWrapper qw =
        new QueryWrapper()
            .eq(DungeonProgress::getUserId, userId)
            .eq(DungeonProgress::getDungeonId, dungeonId);
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  public List<DungeonProgress> findByUserIdAndDungeonIds(Long userId, List<Long> dungeonIds) {
    if (dungeonIds == null || dungeonIds.isEmpty()) return List.of();
    QueryWrapper qw =
        new QueryWrapper()
            .eq(DungeonProgress::getUserId, userId)
            .in(DungeonProgress::getDungeonId, dungeonIds);
    return mapper.selectListByQuery(qw);
  }
}
