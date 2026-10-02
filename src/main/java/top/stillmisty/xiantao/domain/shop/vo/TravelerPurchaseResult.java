package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** 旅行商人购买结果 — 供 LLM 工具返回给模型。 */
public record TravelerPurchaseResult(
    @JsonPropertyDescription("购买的货物名称") String itemName,
    @JsonPropertyDescription("购买数量") int quantity,
    @JsonPropertyDescription("总价（灵石）") long totalPrice,
    @JsonPropertyDescription("该货物剩余库存") int remainingStock) {}
