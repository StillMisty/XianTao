package top.stillmisty.xiantao.service.inventory.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.handler.ItemUseHandler;
import top.stillmisty.xiantao.service.pill.PillConsumptionService;

@Component
@RequiredArgsConstructor
public class PillUseHandler implements ItemUseHandler {

  private final PillConsumptionService pillConsumptionService;

  @Override
  public boolean supports(ItemType type) {
    return type == ItemType.POTION;
  }

  @Override
  public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
    return pillConsumptionService.takePill(userId, item.getName());
  }
}
