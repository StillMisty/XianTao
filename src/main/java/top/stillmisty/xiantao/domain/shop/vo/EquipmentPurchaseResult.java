package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record EquipmentPurchaseResult(
    @JsonPropertyDescription("购买是否成功") boolean success,
    @JsonPropertyDescription("装备名称") String equipmentName,
    @JsonPropertyDescription("稀有度") String rarity,
    @JsonPropertyDescription("价格（灵石）") long price,
    @JsonPropertyDescription("装备描述") String description) {}
