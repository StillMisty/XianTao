package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SellResult(
    @JsonPropertyDescription("交易是否成功") boolean success,
    @JsonPropertyDescription("成交价格（灵石）") long price,
    @JsonPropertyDescription("物品名称") String itemName,
    @JsonPropertyDescription("交易结果消息") String message) {}
