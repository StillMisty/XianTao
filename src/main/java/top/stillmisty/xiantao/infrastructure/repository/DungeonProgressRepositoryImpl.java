package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonProgressRepository;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonProgressMapper;

@Repository
@RequiredArgsConstructor
public class DungeonProgressRepositoryImpl implements DungeonProgressRepository {

  private final DungeonProgressMapper mapper;

  @Override
  public DungeonProgress save(DungeonProgress progress) {
    mapper.insertOrUpdateSelective(progress);
    return progress;
  }

  @Override
  public Optional<DungeonProgress> findByUserIdAndDungeonId(Long userId, Long dungeonId) {
    QueryWrapper qw =
        new QueryWrapper()
            .eq(DungeonProgress::getUserId, userId)
            .eq(DungeonProgress::getDungeonId, dungeonId);
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }
}
