package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 从掌柜购买堆叠物品的操作结果。
 *
 * <p>用灵石购买丹药/材料等堆叠物品，{@code quantity} 是购买数量，{@code totalPrice} 是总价格（灵石）。
 */
public record PurchaseResult(
    @JsonPropertyDescription("购买的物品名称") String itemName,
    @JsonPropertyDescription("购买数量") int quantity,
    @JsonPropertyDescription("总价格（灵石）") long totalPrice) {}
