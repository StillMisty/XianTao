package top.stillmisty.xiantao.domain.sect.vo;

public record SectTaskVO(
    Long taskId,
    String taskType,
    String taskTypeName,
    String targetName,
    Integer requiredCount,
    Integer myProgress,
    Integer contributionReward,
    Boolean completed) {}
