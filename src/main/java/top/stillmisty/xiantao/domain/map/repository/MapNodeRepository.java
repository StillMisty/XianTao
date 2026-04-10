package top.stillmisty.xiantao.domain.map.repository;

import top.stillmisty.xiantao.domain.map.entity.MapNode;
import top.stillmisty.xiantao.domain.map.enums.MapType;

import java.util.List;
import java.util.Optional;

/**
 * 地图节点仓储接口
 */
public interface MapNodeRepository {

    /**
     * 保存地图节点
     */
    MapNode save(MapNode mapNode);

    /**
     * 根据 ID 查找地图节点
     */
    Optional<MapNode> findById(Long id);

    /**
     * 根据名称查找地图节点
     */
    Optional<MapNode> findByName(String name);

    /**
     * 查找所有地图节点
     */
    List<MapNode> findAll();

    /**
     * 根据类型查找地图节点
     */
    List<MapNode> findByType(MapType mapType);

    /**
     * 根据等级范围查找地图节点
     */
    List<MapNode> findByLevelRange(Integer minLevel, Integer maxLevel);

    /**
     * 检查地图名称是否存在
     */
    boolean existsByName(String name);
}
