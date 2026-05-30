package top.stillmisty.xiantao.domain.monster.vo;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record CombatLogEntry(
    int round,
    int sequence,
    String attackerName,
    String defenderName,
    AttackType attackType,
    @Nullable String skillName,
    @Nullable List<String> effects,
    boolean buffApplied,
    @Nullable String buffTarget,
    int damageDealt,
    int defenderHpBefore,
    int defenderHpAfter,
    boolean isKill) {
  public enum AttackType {
    NORMAL,
    SKILL,
    CONTROLLED
  }

  // convenience: fully specified with all fields
}
