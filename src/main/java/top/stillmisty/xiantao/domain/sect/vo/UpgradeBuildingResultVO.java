package top.stillmisty.xiantao.domain.sect.vo;

public record UpgradeBuildingResultVO(
    String buildingTypeCode,
    String buildingName,
    int oldLevel,
    int newLevel,
    long cost,
    long remainingFunds) {}
