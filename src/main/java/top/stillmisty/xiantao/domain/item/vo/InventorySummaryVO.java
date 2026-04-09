package top.stillmisty.xiantao.domain.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

import java.util.Map;

/**
 * 背包摘要 VO
 * 用于简化展示，防止刷屏
 */
@Data
@Builder
public class InventorySummaryVO {

    /**
     * 背包最大容量
     */
    private Integer capacity;

    /**
     * 已使用格子数
     */
    private Integer usedSlots;

    /**
     * 装备按品质分组统计
     * 格式: {"破旧": 5, "普通": 10, "稀有": 3, "史诗": 1, "传说": 0}
     */
    @JsonProperty("equipment_by_quality")
    private Map<String, Integer> equipmentByQuality;

    /**
     * 可堆叠物品按类型统计
     */
    @JsonProperty("stackable_item_count")
    private Map<ItemType, Integer> stackableItemCount;

    /**
     * 铜币数量
     */
    @JsonProperty("coins")
    private Long coins;

    /**
     * 灵石数量
     */
    @JsonProperty("spirit_stones")
    private Long spiritStones;
}
