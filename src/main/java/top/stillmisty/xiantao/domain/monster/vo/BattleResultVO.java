package top.stillmisty.xiantao.domain.monster.vo;

import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record BattleResultVO(
    String winner,
    int rounds,
    Map<String, HpChange> playerHpChange,
    List<SkillProc> skillProcs,
    List<CombatLogEntry> combatLog) {

  /** 未分出胜负时的 winner 值 */
  public static final String DRAW = "DRAW";

  /** 指定队伍是否获胜（平局对任何队伍都返回 false）。 */
  public boolean winnerIs(String teamName) {
    return teamName.equals(winner);
  }

  /** 是否平局 */
  public boolean isDraw() {
    return DRAW.equals(winner);
  }
}
