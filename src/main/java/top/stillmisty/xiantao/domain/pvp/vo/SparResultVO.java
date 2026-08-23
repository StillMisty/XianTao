package top.stillmisty.xiantao.domain.pvp.vo;

import java.util.List;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;

public record SparResultVO(
    String attackerNickname,
    String defenderNickname,
    boolean attackerWon,
    boolean draw,
    List<CombatLogEntry> combatLog,
    List<HpStatus> attackerHpStatus,
    List<HpStatus> defenderHpStatus) {

  public record HpStatus(String name, int hp, int maxHp) {}
}
