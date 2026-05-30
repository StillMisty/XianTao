package top.stillmisty.xiantao.domain.user.vo;

import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;

/** 突破结果 VO */
public record BreakthroughResult(
    boolean success,
    String message,
    @Nullable Double successRate,
    Integer newLevel,
    String realmDisplay,
    boolean isMajorBreakthrough,
    Integer failCount,
    @Nullable Double nextBreakthroughRate,
    @Nullable UserStatusVO userStatus,
    @Nullable BattleResultVO battleResult,
    @Nullable String tribulationTypeName) {

  /** 非战斗突破构造（小境界） */
  public BreakthroughResult(
      boolean success,
      String message,
      @Nullable Double successRate,
      Integer newLevel,
      String realmDisplay,
      boolean isMajorBreakthrough,
      Integer failCount,
      @Nullable Double nextBreakthroughRate,
      @Nullable UserStatusVO userStatus) {
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
