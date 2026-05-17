package top.stillmisty.xiantao.domain.sect.vo;

public record SectBuildingVO(
    Long id,
    String buildingType,
    String buildingName,
    Integer level,
    Integer maxLevel,
    long buildCost,
    String effect) {}
