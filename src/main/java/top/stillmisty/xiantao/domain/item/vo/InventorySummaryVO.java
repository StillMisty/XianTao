package top.stillmisty.xiantao.domain.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/** 背包摘要 VO 用于简化展示，防止刷屏 */
public record InventorySummaryVO(
    @JsonProperty("equipment_by_quality") Map<String, Integer> equipmentByQuality,
    @JsonProperty("stackable_item_count") Map<ItemType, Integer> stackableItemCount,
    @JsonProperty("spirit_stones") Long spiritStones) {}
