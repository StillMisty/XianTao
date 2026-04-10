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

    @Data
    @Builder
    public static class AttributeChange {
        private int strChange;
        private int conChange;
        private int agiChange;
        private int wisChange;
        private int attackChange;
        private int defenseChange;
        private int maxHpChange;
    }
}
