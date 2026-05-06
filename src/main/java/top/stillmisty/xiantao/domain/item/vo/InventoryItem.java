package top.stillmisty.xiantao.domain.item.vo;

import top.stillmisty.xiantao.domain.item.enums.ItemType;

/** 背包物品条目 */
public record InventoryItem(Long itemId, ItemType itemType, String name, Integer quantity) {
  /** 创建便捷方法 */
  public static InventoryItem forStackable(
      Long templateId, ItemType itemType, String name, int quantity) {
    return new InventoryItem(templateId, itemType, name, quantity);
  }
}
