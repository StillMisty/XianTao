package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 出售物品给掌柜的操作结果。
 *
 * <p>向掌柜卖出物品，{@code price} 为成交价格（灵石），{@code itemName} 为售出物品名称。
 * 价格必须在掌柜估价（appraiseItem）和砍价（negotiatePrice）的范围内。
 */
public record SellResult(
    @JsonPropertyDescription("成交价格（灵石）") long price,
    @JsonPropertyDescription("售出的物品名称") String itemName) {}
