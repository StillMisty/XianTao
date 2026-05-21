package top.stillmisty.xiantao.service.combat;

import java.util.List;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.monster.vo.SkillProc;

/** 单次遇怪结果 */
public record EncounterResult(
    boolean playerWon,
    long expGained,
    int kills,
    int rounds,
    boolean enlightenmentTriggered,
    List<DropItem> drops,
    List<CombatLogEntry> logs,
    List<SkillProc> skillProcs,
    boolean isHighlight,
    String monsterName) {

  public static EncounterResult lost() {
    return new EncounterResult(false, 0, 0, 0, false, List.of(), List.of(), List.of(), false, null);
  }
}
