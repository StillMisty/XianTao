package top.stillmisty.xiantao.domain.forge.vo;

import java.util.Map;
import top.stillmisty.xiantao.domain.item.enums.Rarity;

/** 锻造结果VO */
public record ForgingResultVO(
    boolean success,
    String message,
    Long equipmentId,
    String equipmentName,
    Rarity rarity,
    Double qualityScore,
    Map<String, Integer> usedMaterials,
    Map<String, Integer> materialTotals) {}
