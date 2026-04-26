package top.stillmisty.xiantao.domain.map.repository;

import top.stillmisty.xiantao.domain.map.entity.MapConnection;

import java.util.Optional;

/**
 * 地图连接仓储接口
 */
public interface MapConnectionRepository {

    /**
     * 保存地图连接
     */
    MapConnection save(MapConnection connection);

    /**
     * 根据 ID 查找地图连接
     */
    Optional<MapConnection> findById(Long id);
}
