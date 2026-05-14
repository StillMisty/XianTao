package top.stillmisty.xiantao.domain.forge.vo;

import java.util.Map;

/** 强化结果VO */
public record EnhanceResultVO(
    boolean success,
    String message,
    Long equipmentId,
    String equipmentName,
    int newForgeLevel,
    int previousForgeLevel,
    double successRate,
    int spiritStoneCost,
    Map<String, Integer> usedMaterials,
    String milestoneReward) {}
