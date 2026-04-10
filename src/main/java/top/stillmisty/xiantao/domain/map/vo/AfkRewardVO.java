package top.stillmisty.xiantao.domain.map.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 挂机奖励 VO
 */
@Data
@Builder
public class AfkRewardVO {
    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 挂机地图 ID
     */
    private Long mapId;

    /**
     * 挂机地图名称
     */
    private String mapName;

    /**
     * 挂机时长（分钟）
     */
    private Long durationMinutes;

    /**
     * 效率倍率（基于敏捷）
     */
    private Double efficiencyMultiplier;

    /**
     * 获得的铜币
     */
    private Long coins;

    /**
     * 获得的灵石
     */
    private Long spiritStones;

    /**
     * 获得的物品（格式: [{"name": "毒龙草", "quantity": 5, "templateId": 1}]）
     */
    private List<Map<String, Object>> items;

    /**
     * 总奖励描述
     */
    private String summary;
}
