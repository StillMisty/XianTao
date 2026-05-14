package top.stillmisty.xiantao.domain.dungeon.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;

public interface DungeonProgressRepository {
  DungeonProgress save(DungeonProgress progress);

  Optional<DungeonProgress> findByUserIdAndDungeonId(Long userId, Long dungeonId);
}
