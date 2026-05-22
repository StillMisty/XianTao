package top.stillmisty.xiantao.domain.sect.vo;

public record BuildResultVO(
    String buildingTypeCode, String buildingName, int level, long cost, long remainingFunds) {}
