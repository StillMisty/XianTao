package top.stillmisty.xiantao.service.combat;

import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.monster.vo.SkillProc;

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
    List<SkillProc> allSkillProcs,
    boolean hasHighlight,
    @Nullable String firstHighlightMonsterName,
    List<CombatLogEntry> firstHighlightLogs,
    List<SkillProc> firstHighlightSkillProcs,
    @Nullable String lastDefeatMonsterName,
    List<CombatLogEntry> lastDefeatLogs) {

  public static CombatSummary empty() {
    return new CombatSummary(
        0, 0, 0, 0, 0, 0, List.of(), List.of(), List.of(), false, null, List.of(), List.of(),
        null, List.of());
  }

  public CombatSummary merge(EncounterResult result) {
    boolean isHighlight = result.isHighlight() && !this.hasHighlight;
    boolean isDefeat = !result.playerWon();
    return new CombatSummary(
        expGained + result.expGained(),
        totalEncounters + 1,
        totalKills + result.kills(),
        defeatCount + (isDefeat ? 1 : 0),
        totalRounds + result.rounds(),
        enlightenmentCount + (result.enlightenmentTriggered() ? 1 : 0),
        concat(allDrops, result.drops()),
        concat(allLogs, result.logs()),
        concat(allSkillProcs, result.skillProcs()),
        this.hasHighlight || result.isHighlight(),
        isHighlight ? result.monsterName() : firstHighlightMonsterName,
        isHighlight ? result.logs() : firstHighlightLogs,
        isHighlight ? result.skillProcs() : firstHighlightSkillProcs,
        isDefeat ? result.monsterName() : lastDefeatMonsterName,
        isDefeat ? result.logs() : lastDefeatLogs);
  }

  private static <T> List<T> concat(List<T> a, List<T> b) {
    if (b.isEmpty()) return a;
    if (a.isEmpty()) return b;
    var result = new java.util.ArrayList<>(a);
    result.addAll(b);
    return result;
  }
}
