package top.stillmisty.xiantao.service.inventory.handler;

import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

/**
 * 物品使用策略接口（策略模式）
 *
 * <p>设计决策：接口定义在 service 层而非 domain 层，因为实现全部在 service/inventory/handler/。 每个 handler 绑定一种
 * ItemType，通过 Map<ItemType, ItemUseHandler> 分发，无需遍历。
 */
public interface ItemUseHandler {

  /** 此 handler 负责的物品类型 */
  ItemType getItemType();

  default boolean consumesInternally() {
    return false;
  }

  String use(Long userId, StackableItem item, ItemTemplate template, String args);
}
