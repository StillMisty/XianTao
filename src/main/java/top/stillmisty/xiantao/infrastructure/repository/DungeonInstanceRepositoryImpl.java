package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.table.DungeonInstanceTableDef;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonInstanceRepository;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonInstanceMapper;

@Repository
@RequiredArgsConstructor
public class DungeonInstanceRepositoryImpl implements DungeonInstanceRepository {

  private final DungeonInstanceMapper mapper;

  @Override
  public DungeonInstance save(DungeonInstance instance) {
    mapper.insertOrUpdateSelective(instance);
    return instance;
  }

  @Override
  public Optional<DungeonInstance> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public Optional<DungeonInstance> findByIdForUpdate(Long id) {
    QueryWrapper qw =
        new QueryWrapper().eq(DungeonInstanceTableDef.DUNGEON_INSTANCE.ID, id).forUpdate();
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  @Override
  public Optional<DungeonInstance> findByLeaderIdAndDungeonIdAndStatus(
      Long leaderId, Long dungeonId, DungeonStatus status) {
    QueryWrapper qw =
        new QueryWrapper()
            .eq(DungeonInstance::getLeaderId, leaderId)
            .eq(DungeonInstance::getDungeonId, dungeonId)
            .eq(DungeonInstance::getStatus, status);
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }
}
