package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.Map;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;

/** 战斗统计累加器 */
public record CombatSummary(
    long expGained,
    int totalEncounters,
    int totalKills,
    int defeatCount,
    int totalRounds,
    int enlightenmentCount,
    List<DropItem> allDrops,
    List<CombatLogEntry> allLogs,
    List<Map<String, Object>> allSkillProcs,
    boolean hasHighlight,
    String firstHighlightMonsterName,
    List<CombatLogEntry> firstHighlightLogs,
    List<Map<String, Object>> firstHighlightSkillProcs) {

  public static CombatSummary empty() {
    return new CombatSummary(
        0, 0, 0, 0, 0, 0, List.of(), List.of(), List.of(), false, null, List.of(), List.of());
  }

  public CombatSummary merge(EncounterResult result) {
    boolean isHighlight = result.isHighlight() && !this.hasHighlight;
    return new CombatSummary(
        expGained + result.expGained(),
        totalEncounters + 1,
        totalKills + result.kills(),
        defeatCount + (result.playerWon() ? 0 : 1),
        totalRounds + result.rounds(),
        enlightenmentCount + (result.enlightenmentTriggered() ? 1 : 0),
        concat(allDrops, result.drops()),
        concat(allLogs, result.logs()),
        concat(allSkillProcs, result.skillProcs()),
        this.hasHighlight || result.isHighlight(),
        isHighlight ? result.monsterName() : firstHighlightMonsterName,
        isHighlight ? result.logs() : firstHighlightLogs,
        isHighlight ? result.skillProcs() : firstHighlightSkillProcs);
  }

  private static <T> List<T> concat(List<T> a, List<T> b) {
    if (b.isEmpty()) return a;
    if (a.isEmpty()) return b;
    var result = new java.util.ArrayList<>(a);
    result.addAll(b);
    return result;
  }
}
