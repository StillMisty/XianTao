package top.stillmisty.xiantao.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.map.entity.MapConnection;
import top.stillmisty.xiantao.domain.map.repository.MapConnectionRepository;
import top.stillmisty.xiantao.infrastructure.mapper.MapConnectionMapper;

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
}
