package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.infrastructure.mapper.MapNodeMapper;

import java.util.List;
import java.util.Optional;

/**
 * 地图节点仓储实现
 */
@Repository
@RequiredArgsConstructor
public class MapNodeRepositoryImpl implements MapNodeRepository {

    private final MapNodeMapper mapNodeMapper;

    @Override
    public MapNode save(MapNode mapNode) {
        mapNodeMapper.insertOrUpdate(mapNode);
        return mapNode;
    }

    @Override
    public Optional<MapNode> findById(Long id) {
        return Optional.ofNullable(mapNodeMapper.selectOneById(id));
    }

    @Override
    public Optional<MapNode> findByName(String name) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapNode::getName, name);
        return Optional.ofNullable(mapNodeMapper.selectOneByQuery(query));
    }

    @Override
    public List<MapNode> findAll() {
        return mapNodeMapper.selectAll();
    }

    @Override
    public List<MapNode> findByType(MapType mapType) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapNode::getMapType, mapType);
        return mapNodeMapper.selectListByQuery(query);
    }

    @Override
    public List<MapNode> findByLevelRange(Integer minLevel, Integer maxLevel) {
        QueryWrapper query = new QueryWrapper()
                .ge(MapNode::getLevelRequirement, minLevel)
                .le(MapNode::getLevelRequirement, maxLevel);
        return mapNodeMapper.selectListByQuery(query);
    }

    @Override
    public boolean existsByName(String name) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapNode::getName, name);
        return mapNodeMapper.selectCountByQuery(query) > 0;
    }
}
