package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.infrastructure.mapper.MapNodeMapper;

/** 地图节点仓储实现 使用 Spring Cache（底层 Caffeine）缓存 MapNode 查询结果 */
@Repository
@RequiredArgsConstructor
public class MapNodeRepository {

  private final MapNodeMapper mapNodeMapper;

  @CacheEvict(value = "mapNodes", allEntries = true)
  public MapNode save(MapNode mapNode) {
    mapNodeMapper.insertOrUpdateSelective(mapNode);
    return mapNode;
  }

  @Cacheable(value = "mapNodes", key = "'id:' + #id")
  public Optional<MapNode> findById(Long id) {
    return Optional.ofNullable(mapNodeMapper.selectOneById(id));
  }

  @Cacheable(value = "mapNodes", key = "'name:' + #name")
  public Optional<MapNode> findByName(String name) {
    QueryWrapper query = new QueryWrapper().eq(MapNode::getName, name);
    return Optional.ofNullable(mapNodeMapper.selectOneByQuery(query));
  }

  @Cacheable(value = "mapNodes", key = "'all'")
  public List<MapNode> findAll() {
    return mapNodeMapper.selectAll();
  }

  @Cacheable(value = "mapNodes", key = "'type:' + #mapType.code")
  public List<MapNode> findByType(MapType mapType) {
    QueryWrapper query = new QueryWrapper().eq(MapNode::getMapType, mapType);
    return mapNodeMapper.selectListByQuery(query);
  }

  public List<MapNode> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return mapNodeMapper.selectListByIds(ids);
  }
}
