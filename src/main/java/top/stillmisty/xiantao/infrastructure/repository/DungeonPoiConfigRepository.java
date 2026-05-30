package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.dungeon.entity.table.DungeonPoiConfigTableDef.DUNGEON_POI_CONFIG;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonPoiConfigMapper;

@Repository
@RequiredArgsConstructor
public class DungeonPoiConfigRepository {

  private final DungeonPoiConfigMapper mapper;

  public DungeonPoiConfig save(DungeonPoiConfig config) {
    mapper.insertOrUpdateSelective(config);
    return config;
  }

  public Optional<DungeonPoiConfig> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<DungeonPoiConfig> findByDungeonId(Long dungeonId) {
    QueryWrapper qw = QueryWrapper.create().where(DUNGEON_POI_CONFIG.DUNGEON_ID.eq(dungeonId));
    return mapper.selectListByQuery(qw);
  }

  public List<DungeonPoiConfig> findByDungeonIdAndArea(Long dungeonId, DungeonArea area) {
    QueryWrapper qw =
        QueryWrapper.create()
            .where(DUNGEON_POI_CONFIG.DUNGEON_ID.eq(dungeonId))
            .and(DUNGEON_POI_CONFIG.AREA.eq(area));
    return mapper.selectListByQuery(qw);
  }
}
