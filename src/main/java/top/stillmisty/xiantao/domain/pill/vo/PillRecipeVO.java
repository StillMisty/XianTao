package top.stillmisty.xiantao.domain.pill.vo;

import top.stillmisty.xiantao.domain.item.entity.ItemProperties;

import java.util.List;

/**
 * 丹方详情VO
 */
public record PillRecipeVO(
        Long recipeTemplateId,
        String recipeName,
        int grade,
        Long resultItemId,
        String resultItemName,
        int resultQuantity,
        List<ItemProperties.ElementRequirement> requirements
) {
}