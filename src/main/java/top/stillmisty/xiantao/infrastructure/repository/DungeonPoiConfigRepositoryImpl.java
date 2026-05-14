package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.repository.DungeonPoiConfigRepository;
import top.stillmisty.xiantao.infrastructure.mapper.DungeonPoiConfigMapper;

@Repository
@RequiredArgsConstructor
public class DungeonPoiConfigRepositoryImpl implements DungeonPoiConfigRepository {

  private final DungeonPoiConfigMapper mapper;

  @Override
  public DungeonPoiConfig save(DungeonPoiConfig config) {
    mapper.insertOrUpdateSelective(config);
    return config;
  }

  @Override
  public Optional<DungeonPoiConfig> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<DungeonPoiConfig> findByDungeonId(Long dungeonId) {
    QueryWrapper qw = new QueryWrapper().eq(DungeonPoiConfig::getDungeonId, dungeonId);
    return mapper.selectListByQuery(qw);
  }

  @Override
  public List<DungeonPoiConfig> findByDungeonIdAndArea(Long dungeonId, DungeonArea area) {
    QueryWrapper qw =
        new QueryWrapper()
            .eq(DungeonPoiConfig::getDungeonId, dungeonId)
            .eq(DungeonPoiConfig::getArea, area);
    return mapper.selectListByQuery(qw);
  }
}
