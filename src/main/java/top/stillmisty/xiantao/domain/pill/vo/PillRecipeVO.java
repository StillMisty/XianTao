package top.stillmisty.xiantao.domain.pill.vo;

import java.util.Map;

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
        Map<String, Map<String, Integer>> requirements
) {
}