package top.stillmisty.xiantao.domain.item.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.forge.vo.ForgingRecipeVO;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.service.ForgingService;

@Component
@RequiredArgsConstructor
public class ForgingBlueprintUseHandler implements ItemUseHandler {

  private final ForgingService forgingService;

  @Override
  public boolean supports(ItemType type, ItemTemplate template) {
    return type == ItemType.FORGING_BLUEPRINT;
  }

  @Override
  public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
    ForgingRecipeVO recipe = forgingService.learnRecipe(userId, item.getName());
    if (recipe != null) {
      return "学习锻造图纸成功：" + recipe.blueprintName();
    }
    return "学习锻造图纸失败，请检查背包中是否有锻造图纸";
  }
}
