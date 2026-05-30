package top.stillmisty.xiantao.service.inventory.handler;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.pill.vo.PillRecipeVO;
import top.stillmisty.xiantao.service.pill.PillRecipeService;

@Component
@RequiredArgsConstructor
public class RecipeScrollUseHandler implements ItemUseHandler {

  private final PillRecipeService pillRecipeService;

  @Override
  public ItemType getItemType() {
    return ItemType.RECIPE_SCROLL;
  }

  @Override
  public String use(Long userId, StackableItem item, @Nullable ItemTemplate template, String args) {
    PillRecipeVO recipe = pillRecipeService.learnRecipeInternal(userId, item.getName());
    return recipe != null ? "学习丹方成功：" + recipe.recipeName() : "学习丹方失败，请检查背包中是否有丹方卷轴";
  }
}
