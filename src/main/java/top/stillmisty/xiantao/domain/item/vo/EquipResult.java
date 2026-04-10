package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

/**
 * 装备穿戴结果 VO
 */
@Data
@Builder
public class EquipResult {

    private boolean success;
    private String message;

    // 装备信息
    private Long equipmentId;
    private String equipmentName;
    private EquipmentSlot slot;
    private String slotName;

    // 被替换的装备 (如果有)
    private Long replacedEquipmentId;
    private String replacedEquipmentName;

    // 穿戴后的属性变化
    private AttributeChange attributeChange;
}
