package top.stillmisty.xiantao.domain.dungeon.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;

public interface DungeonPoiConfigRepository {
  DungeonPoiConfig save(DungeonPoiConfig config);

  Optional<DungeonPoiConfig> findById(Long id);

  List<DungeonPoiConfig> findByDungeonId(Long dungeonId);

  List<DungeonPoiConfig> findByDungeonIdAndArea(Long dungeonId, DungeonArea area);
}
