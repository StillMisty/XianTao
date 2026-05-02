package top.stillmisty.xiantao.domain.item.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.pill.vo.PillRecipeVO;
import top.stillmisty.xiantao.service.PillService;

/**
 * 丹方卷轴使用处理器
 */
@Component
@RequiredArgsConstructor
public class RecipeScrollUseHandler implements ItemUseHandler {

    private final PillService pillService;

    @Override
    public boolean supports(ItemType type, ItemTemplate template) {
        return type == ItemType.RECIPE_SCROLL;
    }

    @Override
    public String use(Long userId, StackableItem item, ItemTemplate template, String args) {
        PillRecipeVO recipe = pillService.learnRecipe(userId, item.getName());
        if (recipe != null) {
            return "学习丹方成功：" + recipe.recipeName();
        }
        return "学习丹方失败，请检查背包中是否有丹方卷轴";
    }
}
