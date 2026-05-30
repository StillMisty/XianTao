package top.stillmisty.xiantao.domain.item.vo;

import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

/** 装备穿戴结果 VO */
public record EquipResult(
    boolean success,
    String message,
    @Nullable Long equipmentId,
    @Nullable String equipmentName,
    @Nullable EquipmentSlot slot,
    @Nullable String slotName,
    @Nullable Long replacedEquipmentId,
    @Nullable String replacedEquipmentName,
    @Nullable AttributeChange attributeChange) {}
