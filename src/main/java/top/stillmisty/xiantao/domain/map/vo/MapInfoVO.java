package top.stillmisty.xiantao.domain.map.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.map.entity.SpecialtyEntry;
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
     * 要求等级
     */
    private Integer levelRequirement;

    /**
     * 相邻地图 ID → 旅行耗时 (JSONB 反序列化后处理)
     * 格式: {"1": 5, "2": 10}
     */
    private Map<String, Integer> neighbors;

    /**
     * 相邻地图名称列表（已解析，用于展示）
     */
    private List<String> adjacentMapNames;

    /**
     * 历练掉落池/特产 (JSONB)
     */
    private List<SpecialtyEntry> specialties;

    /**
     * 旅行事件权重 (JSONB)
     * 格式: {"ambush": 40, "find_treasure": 10} (eventType → weight)
     */
    private Map<String, Integer> travelEvents;

    /**
     * 遇怪列表（已解析怪物名称）
     */
    private List<MonsterInfoVO> monsters;

    /**
     * 地图遇怪信息 VO
     */
    @Data
    @Builder
    public static class MonsterInfoVO {
        /**
         * 怪物模板 ID
         */
        private Long templateId;

        /**
         * 怪物名称
         */
        private String name;

        /**
         * 怪物类型名称
         */
        private String typeName;

        /**
         * 基础等级
         */
        private Integer baseLevel;

        /**
         * 出现权重
         */
        private int weight;

        /**
         * 最少出现数量
         */
        private int minCount;

        /**
         * 最多出现数量
         */
        private int maxCount;
    }
}
