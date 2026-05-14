package top.stillmisty.xiantao.domain.dungeon.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonFirstClear;

public interface DungeonFirstClearRepository {
  DungeonFirstClear save(DungeonFirstClear firstClear);

  Optional<DungeonFirstClear> findByDungeonId(Long dungeonId);
}
