package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.table.DungeonInstanceTableDef;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonInstanceMapper;

@Repository
@RequiredArgsConstructor
public class DungeonInstanceRepository {

  private final DungeonInstanceMapper mapper;

  public DungeonInstance save(DungeonInstance instance) {
    mapper.insertOrUpdateSelective(instance);
    return instance;
  }

  public Optional<DungeonInstance> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public Optional<DungeonInstance> findByIdForUpdate(Long id) {
    QueryWrapper qw =
        QueryWrapper.create().where(DungeonInstanceTableDef.DUNGEON_INSTANCE.ID.eq(id)).forUpdate();
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  public Optional<DungeonInstance> findByLeaderIdAndDungeonIdAndStatus(
      Long leaderId, Long dungeonId, DungeonStatus status) {
    QueryWrapper qw =
        new QueryWrapper()
            .eq(DungeonInstance::getLeaderId, leaderId)
            .eq(DungeonInstance::getDungeonId, dungeonId)
            .eq(DungeonInstance::getStatus, status);
    return Optional.ofNullable(mapper.selectOneByQuery(qw));
  }

  public List<DungeonInstance> findByLeaderIdAndDungeonIdsAndStatus(
      Long leaderId, List<Long> dungeonIds, DungeonStatus status) {
    if (dungeonIds == null || dungeonIds.isEmpty()) return List.of();
    QueryWrapper qw =
        new QueryWrapper()
            .eq(DungeonInstance::getLeaderId, leaderId)
            .in(DungeonInstance::getDungeonId, dungeonIds)
            .eq(DungeonInstance::getStatus, status);
    return mapper.selectListByQuery(qw);
  }

  public List<DungeonInstance> findByLeaderIdsAndDungeonIdAndStatus(
      List<Long> leaderIds, Long dungeonId, DungeonStatus status) {
    if (leaderIds == null || leaderIds.isEmpty()) return List.of();
    QueryWrapper qw =
        new QueryWrapper()
            .in(DungeonInstance::getLeaderId, leaderIds)
            .eq(DungeonInstance::getDungeonId, dungeonId)
            .eq(DungeonInstance::getStatus, status);
    return mapper.selectListByQuery(qw);
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }
}
