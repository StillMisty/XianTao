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
    List<CombatLogEntry> combatLog,
    List<DropItem> drops,
    long expGained,
    String summary) {}
