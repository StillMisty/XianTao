package top.stillmisty.xiantao.domain.map.repository;

import top.stillmisty.xiantao.domain.map.entity.MapConnection;

import java.util.List;
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

    /**
     * 查找从指定地图出发的所有连接
     */
    List<MapConnection> findByFromMapId(Long fromMapId);

    /**
     * 查找到指定地图的所有连接
     */
    List<MapConnection> findByToMapId(Long toMapId);

    /**
     * 查找两个地图之间的连接
     */
    Optional<MapConnection> findByFromAndTo(Long fromMapId, Long toMapId);

    /**
     * 删除指定地图的所有连接
     */
    void deleteByMapId(Long mapId);
}
