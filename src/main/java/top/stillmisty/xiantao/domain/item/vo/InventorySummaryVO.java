package top.stillmisty.xiantao.domain.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/** 背包摘要 VO — 装备列表 + 物品按类型分组 + 灵石 */
public record InventorySummaryVO(
    @JsonProperty("equipment") List<ItemEntry> equipment,
    @JsonProperty("items_by_type") Map<ItemType, List<ItemEntry>> itemsByType,
    @JsonProperty("spirit_stones") Long spiritStones) {}
