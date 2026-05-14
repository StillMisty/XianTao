package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record HaggleResult(
    @JsonPropertyDescription("砍价是否成功") boolean success,
    @JsonPropertyDescription("当前报价（灵石），成功时为降价后的新价格") long currentPrice,
    @JsonPropertyDescription("如果成功，降价幅度（灵石）") long discountAmount,
    @JsonPropertyDescription("结果原因描述") String reason) {}
