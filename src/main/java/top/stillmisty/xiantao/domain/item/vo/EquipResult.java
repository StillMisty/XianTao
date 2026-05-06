package top.stillmisty.xiantao.domain.item.vo;

import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

/** 装备穿戴结果 VO */
public record EquipResult(
    boolean success,
    String message,
    Long equipmentId,
    String equipmentName,
    EquipmentSlot slot,
    String slotName,
    Long replacedEquipmentId,
    String replacedEquipmentName,
    AttributeChange attributeChange) {}
