package top.stillmisty.xiantao.service.inventory.handler;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.service.pill.PillConsumptionService;

@Component
@RequiredArgsConstructor
public class PillUseHandler implements ItemUseHandler {

  private final PillConsumptionService pillConsumptionService;

  @Override
  public ItemType getItemType() {
    return ItemType.POTION;
  }

  @Override
  public String use(Long userId, StackableItem item, @Nullable ItemTemplate template, String args) {
    return pillConsumptionService.takePillInternal(userId, item.getName());
  }
}
