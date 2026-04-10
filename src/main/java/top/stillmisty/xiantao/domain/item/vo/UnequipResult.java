package top.stillmisty.xiantao.domain.item.vo;

import lombok.Builder;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;

/**
 * 卸下装备结果 VO
 */
@Data
@Builder
public class UnequipResult {

    private boolean success;
    private String message;

    private Long equipmentId;
    private String equipmentName;
    private EquipmentSlot slot;
    private String slotName;

    // 卸下后的属性变化
    private AttributeChange attributeChange;
}
