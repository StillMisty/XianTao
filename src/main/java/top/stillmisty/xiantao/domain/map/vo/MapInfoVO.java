package top.stillmisty.xiantao.domain.map.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.map.enums.MapType;

import java.util.List;
import java.util.Map;

/**
 * 地图信息 VO
 */
@Data
@Builder
public class MapInfoVO {
    /**
     * 地图 ID
     */
    private Long id;

    /**
     * 地图名称
     */
    private String name;

    /**
     * 地图描述
     */
    private String description;

    /**
     * 地图类型
     */
    private MapType mapType;

    /**
     * 地图类型名称
     */
    private String mapTypeName;

    /**
     * 推荐等级
     */
    private Integer levelRequirement;

    /**
     * 旅行耗时（分钟）
     */
    private Integer travelTimeMinutes;

    /**
     * 相邻地图 (JSONB)
     * 格式: {"黑金主城": 5, "枯骨林": 10}
     */
    private Map<String, Integer> neighbors;

    /**
     * 相邻地图名称列表
     */
    private List<String> adjacentMapNames;

    /**
     * 挂机掉落池/特产 (JSONB)
     * 格式: [{"name": "毒龙草", "weight": 30}, {"name": "铁矿石", "weight": 50}]
     */
    private List<Map<String, Object>> specialties;

    /**
     * 旅行事件权重 (JSONB)
     * 格式: [{"eventType": "ambush", "weight": 40}, {"eventType": "find_treasure", "weight": 10}]
     */
    private List<Map<String, Object>> travelEvents;
}
