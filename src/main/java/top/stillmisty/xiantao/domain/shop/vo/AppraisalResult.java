package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AppraisalResult(
    @JsonPropertyDescription("是否可回收") boolean tradable,
    @JsonPropertyDescription("基准收购价（灵石），首次报价必须等于此值") long basePrice,
    @JsonPropertyDescription("最低可能收购价（灵石）") long minPrice,
    @JsonPropertyDescription("最高可能收购价（灵石）") long maxPrice,
    @JsonPropertyDescription("物品名称") String itemName,
    @JsonPropertyDescription("估价描述，供 LLM 参考") String description) {}
