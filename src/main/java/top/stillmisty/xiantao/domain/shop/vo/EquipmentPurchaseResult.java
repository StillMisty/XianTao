package top.stillmisty.xiantao.domain.shop.vo;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 从掌柜购买装备的操作结果。
 *
 * <p>用灵石购买装备，{@code rarity} 为随机品质（COMMON/UNCOMMON/RARE/EPIC/LEGENDARY）， {@code price}
 * 为价格（灵石）。装备品质和具体词缀随机生成。
 */
public record EquipmentPurchaseResult(
    @JsonPropertyDescription("购买的装备名称") String equipmentName,
    @JsonPropertyDescription("装备品质：COMMON=普通, UNCOMMON=精良, RARE=稀有, EPIC=史诗, LEGENDARY=传说")
        String rarity,
    @JsonPropertyDescription("成交价格（灵石）") long price,
    @JsonPropertyDescription("装备描述（含词缀信息）") String description) {}
