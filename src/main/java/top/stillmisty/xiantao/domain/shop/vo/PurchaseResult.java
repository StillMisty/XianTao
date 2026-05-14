package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PurchaseResult(
    @JsonPropertyDescription("购买是否成功") boolean success,
    @JsonPropertyDescription("物品名称") String itemName,
    @JsonPropertyDescription("购买数量") int quantity,
    @JsonPropertyDescription("总价（灵石）") long totalPrice,
    @JsonPropertyDescription("购买结果消息") String message) {}
