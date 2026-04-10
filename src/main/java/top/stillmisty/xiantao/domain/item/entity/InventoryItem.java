package top.stillmisty.xiantao.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/**
 * 背包物品条目
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    /**
     * 物品实例ID (对于装备) 或 模板ID (对于堆叠物品)
     */
    private Long itemId;

    /**
     * 物品类型
     */
    private ItemType itemType;

    /**
     * 物品名称
     */
    private String name;

    /**
     * 数量 (堆叠物品使用)
     */
    private Integer quantity;

    /**
     * 创建便捷方法 - 装备类物品
     */
    public static InventoryItem forEquipment(Long equipmentId, String name) {
        return new InventoryItem(equipmentId, ItemType.EQUIPMENT, name, 1);
    }

    /**
     * 创建便捷方法 - 堆叠类物品
     */
    public static InventoryItem forStackable(Long templateId, ItemType itemType,
                                              String name, int quantity) {
        return new InventoryItem(templateId, itemType, name, quantity);
    }
}
