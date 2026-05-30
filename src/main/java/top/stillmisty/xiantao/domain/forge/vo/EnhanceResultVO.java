package top.stillmisty.xiantao.domain.forge.vo;

import java.util.Map;
import org.jspecify.annotations.Nullable;

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
    @Nullable Map<String, Integer> usedMaterials,
    @Nullable String milestoneReward) {}
