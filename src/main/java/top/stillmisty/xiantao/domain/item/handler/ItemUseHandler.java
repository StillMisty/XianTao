package top.stillmisty.xiantao.domain.item.handler;

import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

public interface ItemUseHandler {

  boolean supports(ItemType type);

  default boolean consumesInternally() {
    return false;
  }

  String use(Long userId, StackableItem item, ItemTemplate template, String args);
}
