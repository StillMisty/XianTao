package top.stillmisty.xiantao.domain.map.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 探索结果 VO
 */
@Data
@Builder
public class ExplorationResultVO {
    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 探索地图 ID
     */
    private Long mapId;

    /**
     * 探索地图名称
     */
    private String mapName;

    /**
     * 玩家智慧值
     */
    private Integer wisdom;

    /**
     * 消耗的时间（分钟）
     */
    private Integer timeCostMinutes;

    /**
     * 消耗的体力
     */
    private Integer staminaCost;

    /**
     * 探索事件类型
     */
    private String eventType;

    /**
     * LLM 生成的描述文本
     */
    private String description;

    /**
     * 发现的物品（格式: [{"name": "毒龙草", "quantity": 3, "templateId": 1}]）
     */
    private List<Map<String, Object>> foundItems;

    /**
     * 发现的配方 ID（如果有）
     */
    private Long recipeId;

    /**
     * 发现的配方名称（如果有）
     */
    private String recipeName;

    /**
     * 获得的经验值
     */
    private Long expGained;
}
