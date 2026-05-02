package top.stillmisty.xiantao.domain.monster.vo;

import java.util.List;

public record CombatLogEntry(
        int round,
        int sequence,
        String attackerName,
        String defenderName,
        AttackType attackType,
        String skillName,
        List<String> effects,
        boolean buffApplied,
        String buffTarget,
        int damageDealt,
        int defenderHpBefore,
        int defenderHpAfter,
        boolean isKill
) {
    public enum AttackType {
        NORMAL, SKILL, CONTROLLED
    }
}
