package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.mapper.MapNodeMapper;

import java.util.List;
import java.util.Optional;

/**
 * 地图节点仓储实现
 * 使用 Spring Cache（底层 Caffeine）缓存 MapNode 查询结果
 */
@Repository
@RequiredArgsConstructor
public class MapNodeRepositoryImpl implements MapNodeRepository {

    private final MapNodeMapper mapNodeMapper;

    @Override
    @CacheEvict(value = "mapNodes", allEntries = true)
    public MapNode save(MapNode mapNode) {
        mapNodeMapper.insertOrUpdate(mapNode);
        return mapNode;
    }

    @Override
    @Cacheable(value = "mapNodes", key = "'id:' + #id")
    public Optional<MapNode> findById(Long id) {
        return Optional.ofNullable(mapNodeMapper.selectOneById(id));
    }

    @Override
    @Cacheable(value = "mapNodes", key = "'name:' + #name")
    public Optional<MapNode> findByName(String name) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapNode::getName, name);
        return Optional.ofNullable(mapNodeMapper.selectOneByQuery(query));
    }

    @Override
    @Cacheable(value = "mapNodes", key = "'all'")
    public List<MapNode> findAll() {
        return mapNodeMapper.selectAll();
    }

    @Override
    @Cacheable(value = "mapNodes", key = "'type:' + #mapType.code")
    public List<MapNode> findByType(MapType mapType) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapNode::getMapType, mapType);
        return mapNodeMapper.selectListByQuery(query);
    }
}
