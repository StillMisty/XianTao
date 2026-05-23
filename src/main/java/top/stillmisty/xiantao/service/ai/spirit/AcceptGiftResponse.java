package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 接受礼物操作结果。
 *
 * <p>收下主人赠送的物品后，好感度会变化（喜欢的物品+好感，不喜欢的-好感）。 {@code reaction} 是地灵收到礼物后的情绪化反应文本，可直接复述给主人。
 */
public record AcceptGiftResponse(
    @JsonPropertyDescription("收到的礼物物品名称") String itemName,
    @JsonPropertyDescription("好感度变化值：正数表示上升，负数表示下降") int affectionChange,
    @JsonPropertyDescription("地灵收到礼物后的反应描述，可直接告诉主人") String reaction) {}
