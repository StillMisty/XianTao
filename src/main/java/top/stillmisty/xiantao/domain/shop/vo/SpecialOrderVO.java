package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 调货订单视图 — 供掌柜对话工具返回给 LLM。
 *
 * <p>调货流程：下单收取总价 10% 定金 → 按价格档等待 2~12 小时 → 到货后补尾款取货。
 */
public record SpecialOrderVO(
    @JsonPropertyDescription("调货订单编号") long orderId,
    @JsonPropertyDescription("物品名称") String itemName,
    @JsonPropertyDescription("订单状态码：PENDING-等待调货 READY-已到货 COLLECTED-已取货 CANCELLED-已取消")
        String status,
    @JsonPropertyDescription("订单状态说明") String statusName,
    @JsonPropertyDescription("单价（灵石）") long unitPrice,
    @JsonPropertyDescription("数量") int quantity,
    @JsonPropertyDescription("已付定金（灵石）") long deposit,
    @JsonPropertyDescription("取货还需补的尾款（灵石）") long tailPayment,
    @JsonPropertyDescription("调货时长（小时）") int sourcingHours,
    @JsonPropertyDescription("预计到货时间") String expectedReadyAt,
    @JsonPropertyDescription("是否已到货可补尾款取货") boolean canCollect,
    @JsonPropertyDescription("取消订单时退还的定金（灵石），其余情况为 0") long refundedDeposit) {}
