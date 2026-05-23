package top.stillmisty.xiantao.service.inventory.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.handler.ItemUseHandler;
import top.stillmisty.xiantao.service.forging.ForgingService;

@Component
@RequiredArgsConstructor
public class ForgingBlueprintUseHandler implements ItemUseHandler {

  private final ForgingService forgingService;

  @Override
  public boolean supports(ItemType type) {
    return type == ItemType.FORGING_BLUEPRINT;
  }

  @Override
  public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
    var blueprint = forgingService.learnRecipe(userId, item.getName());
    return blueprint != null ? "学习锻造图纸成功：" + blueprint.blueprintName() : "学习锻造图纸失败，请检查背包中是否有锻造图纸";
  }
}
