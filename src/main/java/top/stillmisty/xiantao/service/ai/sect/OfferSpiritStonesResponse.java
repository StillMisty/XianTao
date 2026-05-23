package top.stillmisty.xiantao.service.ai.sect;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 捐献灵石操作结果。
 *
 * <p>弟子向宗门捐献灵石以换取贡献值。兑换比例为 10:1（10灵石=1贡献值）， 最低捐献额 1000 灵石。
 */
public record OfferSpiritStonesResponse(
    @JsonPropertyDescription("捐献的灵石数量") long amount,
    @JsonPropertyDescription("此次捐献获得的贡献值（约 amount/10）") int contributionGained) {}
