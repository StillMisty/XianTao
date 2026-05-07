package top.stillmisty.xiantao.domain.pill.vo;

import java.util.List;
import top.stillmisty.xiantao.domain.item.entity.ElementRequirement;

/** 丹方详情VO */
public record PillRecipeVO(
    Long recipeTemplateId,
    String recipeName,
    int grade,
    Long resultItemId,
    String resultItemName,
    int resultQuantity,
    List<ElementRequirement> requirements) {}
