package top.stillmisty.xiantao.domain.forge.vo;

import java.util.Map;
import top.stillmisty.xiantao.domain.item.entity.ElementRange;

/** 锻造图纸详情VO */
public record ForgingRecipeVO(
    Long blueprintTemplateId,
    String blueprintName,
    int grade,
    Long equipmentTemplateId,
    String equipmentTemplateName,
    Map<String, ElementRange> requirements) {}
