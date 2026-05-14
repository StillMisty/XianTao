package top.stillmisty.xiantao.domain.dungeon.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;

public interface DungeonInstanceRepository {
  DungeonInstance save(DungeonInstance instance);

  Optional<DungeonInstance> findById(Long id);

  Optional<DungeonInstance> findByLeaderIdAndDungeonIdAndStatus(
      Long leaderId, Long dungeonId, DungeonStatus status);

  void deleteById(Long id);
}
