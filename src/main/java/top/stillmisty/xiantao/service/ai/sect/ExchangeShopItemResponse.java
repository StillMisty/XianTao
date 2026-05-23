package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 宗门商店兑换结果。
 *
 * <p>使用贡献值兑换指定商品。成功后商品进入背包，贡献值扣除。 {@code remainingContribution} 为兑换后的剩余贡献值余额。
 */
public record ExchangeShopItemResponse(
    @JsonPropertyDescription("兑换的商品编号") long shopItemId,
    @JsonPropertyDescription("兑换到的物品名称") String itemName,
    @JsonPropertyDescription("兑换后剩余的贡献值") int remainingContribution) {}
