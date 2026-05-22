package top.stillmisty.xiantao.domain.shop.vo;

public record EquipmentPurchaseResult(
    String equipmentName, String rarity, long price, String description, String error) {}
