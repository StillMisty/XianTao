package top.stillmisty.xiantao.domain.user.vo;

/** 突破结果 VO */
public record BreakthroughResult(
    boolean success,
    String message,
    Double successRate,
    Integer newLevel,
    String realmDisplay,
    boolean isMajorBreakthrough,
    Integer failCount,
    Double nextBreakthroughRate,
    UserStatusVO userStatus) {}
