package top.stillmisty.xiantao.domain.fudi.vo;

import java.time.LocalDateTime;

/** 灵田地块值对象 */
public record FarmCellVO(
    Integer cellId,
    Integer cellLevel,
    Integer cropId,
    String cropName,
    LocalDateTime plantTime,
    LocalDateTime matureTime,
    Double growthProgress,
    Boolean isMature,
    Double baseGrowthHours,
    Double actualGrowthHours,
    Integer harvestCount,
    Integer maxHarvest,
    boolean isPerennial) {}
