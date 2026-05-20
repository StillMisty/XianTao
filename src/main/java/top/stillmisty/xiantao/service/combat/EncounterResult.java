package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;

/** 单次遇怪结果 */
public record EncounterResult(
    boolean playerWon,
    long expGained,
    int kills,
    int rounds,
    boolean enlightenmentTriggered,
    List<DropItem> drops,
    List<CombatLogEntry> logs,
    List<Map<String, Object>> skillProcs,
    boolean isHighlight) {

  @SuppressWarnings("unchecked")
  public static EncounterResult won(
      long expGained,
      int kills,
      int rounds,
      boolean enlightenment,
      Object dropsObj,
      List<CombatLogEntry> logs,
      List<Map<String, Object>> skillProcs,
      boolean isHighlight) {
    List<DropItem> drops;
    try {
      drops = (List<DropItem>) dropsObj;
    } catch (ClassCastException e) {
      drops = List.of();
    }
    return new EncounterResult(
        true, expGained, kills, rounds, enlightenment, drops, logs, skillProcs, isHighlight);
  }

  public static EncounterResult lost() {
    return new EncounterResult(false, 0, 0, 0, false, List.of(), List.of(), List.of(), false);
  }
}
