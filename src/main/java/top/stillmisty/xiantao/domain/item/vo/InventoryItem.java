package top.stillmisty.xiantao.domain.item.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/** 背包物品条目 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

  /** 模板ID */
  private Long itemId;

  /** 物品类型 */
  private ItemType itemType;

  /** 物品名称 */
  private String name;

  /** 数量 */
  private Integer quantity;

  /** 创建便捷方法 */
  public static InventoryItem forStackable(
      Long templateId, ItemType itemType, String name, int quantity) {
    return new InventoryItem(templateId, itemType, name, quantity);
  }
}
