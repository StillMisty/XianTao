package top.stillmisty.xiantao.domain.monster.vo;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record BattleResultVO(
        String winner,
        int rounds,
        Map<String, Object> playerHpChange,
        List<Map<String, Object>> beastHpChanges,
        List<Map<String, Object>> monsterHpChanges,
        List<Map<String, Object>> damageDealt,
        List<Map<String, Object>> skillProcs,
        List<CombatLogEntry> combatLog,
        List<DropItem> drops,
        long expGained,
        String summary
) {}
