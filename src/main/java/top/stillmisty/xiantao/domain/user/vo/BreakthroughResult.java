package top.stillmisty.xiantao.domain.user.vo;

import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;

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
    UserStatusVO userStatus,
    BattleResultVO battleResult,
    String tribulationTypeName) {

  /** 非战斗突破构造（小境界） */
  public BreakthroughResult(
      boolean success,
      String message,
      Double successRate,
      Integer newLevel,
      String realmDisplay,
      boolean isMajorBreakthrough,
      Integer failCount,
      Double nextBreakthroughRate,
      UserStatusVO userStatus) {
    this(
        success,
        message,
        successRate,
        newLevel,
        realmDisplay,
        isMajorBreakthrough,
        failCount,
        nextBreakthroughRate,
        userStatus,
        null,
        null);
  }
}
