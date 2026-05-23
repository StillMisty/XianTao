package top.stillmisty.xiantao.service.inventory.handler;

import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.handler.ItemUseHandler;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;

@Component
public class BeastEssenceUseHandler implements ItemUseHandler {

  @Override
  public boolean supports(ItemType type) {
    return type == ItemType.BEAST_ESSENCE;
  }

  @Override
  public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
    throw new BusinessException(ErrorCode.ITEM_CANNOT_USE);
  }
}
