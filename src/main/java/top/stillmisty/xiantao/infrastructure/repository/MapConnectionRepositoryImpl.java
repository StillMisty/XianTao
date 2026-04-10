package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.map.entity.MapConnection;
import top.stillmisty.xiantao.domain.map.repository.MapConnectionRepository;
import top.stillmisty.xiantao.infrastructure.mapper.MapConnectionMapper;

import java.util.List;
import java.util.Optional;

/**
 * 地图连接仓储实现
 */
@Repository
@RequiredArgsConstructor
public class MapConnectionRepositoryImpl implements MapConnectionRepository {

    private final MapConnectionMapper mapConnectionMapper;

    @Override
    public MapConnection save(MapConnection connection) {
        mapConnectionMapper.insertOrUpdate(connection);
        return connection;
    }

    @Override
    public Optional<MapConnection> findById(Long id) {
        return Optional.ofNullable(mapConnectionMapper.selectOneById(id));
    }

    @Override
    public List<MapConnection> findByFromMapId(Long fromMapId) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapConnection::getFromMapId, fromMapId);
        return mapConnectionMapper.selectListByQuery(query);
    }

    @Override
    public List<MapConnection> findByToMapId(Long toMapId) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapConnection::getToMapId, toMapId);
        return mapConnectionMapper.selectListByQuery(query);
    }

    @Override
    public Optional<MapConnection> findByFromAndTo(Long fromMapId, Long toMapId) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapConnection::getFromMapId, fromMapId)
                .eq(MapConnection::getToMapId, toMapId);
        return Optional.ofNullable(mapConnectionMapper.selectOneByQuery(query));
    }

    @Override
    public void deleteByMapId(Long mapId) {
        QueryWrapper query = new QueryWrapper()
                .eq(MapConnection::getFromMapId, mapId);
        mapConnectionMapper.deleteByQuery(query);
    }
}
