package top.stillmisty.xiantao.domain.dungeon.vo;

public record LootPoolEntry(
    String type, // ITEM or EQUIPMENT
    Long templateId,
    int weight,
    int minQty,
    int maxQty) {}
