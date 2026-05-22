package top.stillmisty.xiantao.domain.sect.vo;

public record UpgradeSectResultVO(
    int newLevel, int newMaxMembers, long cost, long remainingFunds) {}
