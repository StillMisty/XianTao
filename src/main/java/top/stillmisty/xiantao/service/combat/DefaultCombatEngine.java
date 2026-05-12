package top.stillmisty.xiantao.service.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.BeastCombatant;
import top.stillmisty.xiantao.domain.monster.Buff;
import top.stillmisty.xiantao.domain.monster.BuffManager;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.Team;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultCombatEngine implements CombatEngine {

  private final DamageCalculator damageCalculator;

  private static String skillKey(Combatant attacker, Skill skill) {
    return attacker.getId() + ":" + skill.getId();
  }

  @Override
  public BattleResultVO simulate(BattleContext context) {
    Team teamA = context.getTeamA();
    Team teamB = context.getTeamB();
    int maxRounds = context.getMaxRounds();

    int round = 0;
    List<CombatLogEntry> combatLog = new ArrayList<>();
    Map<String, Integer> damageDealt = new LinkedHashMap<>();
    Map<String, Integer> skillProcs = new LinkedHashMap<>();
    Map<String, Integer> skillCooldowns = new LinkedHashMap<>();
    BuffManager buffManager = new BuffManager();

    Map<String, Integer> initialHpA = captureHp(teamA);
    Map<String, Integer> initialHpB = captureHp(teamB);

    String winner = "DRAW";
    while (round < maxRounds) {
      round++;

      processOverTimeEffects(teamA, buffManager);
      processOverTimeEffects(teamB, buffManager);

      List<Combatant> turnOrder = buildTurnOrder(teamA, teamB, buffManager);

      int sequence = 0;
      for (Combatant attacker : turnOrder) {
        if (!attacker.isAlive()) continue;

        if (buffManager.hasControl(attacker.getId())) {
          combatLog.add(
              new CombatLogEntry(
                  round,
                  ++sequence,
                  attacker.getName(),
                  attacker.getName(),
                  CombatLogEntry.AttackType.CONTROLLED,
                  null,
                  null,
                  false,
                  null,
                  0,
                  attacker.getHp(),
                  attacker.getHp(),
                  false));
          continue;
        }

        Team attackerTeam = teamA.members().contains(attacker) ? teamA : teamB;
        Team defenderTeam = attackerTeam == teamA ? teamB : teamA;
        sequence++;

        Skill selectedSkill = selectSkill(attacker, skillCooldowns, buffManager);
        Combatant defender = selectTarget(defenderTeam);
        if (defender == null) break;

        CombatLogEntry logEntry =
            resolveAction(
                attacker,
                defender,
                selectedSkill,
                skillCooldowns,
                skillProcs,
                damageDealt,
                buffManager,
                round,
                sequence);
        combatLog.add(logEntry);

        if (defenderTeam.isAllDead()) break;
      }

      tickCooldowns(skillCooldowns);

      if (teamB.isAllDead()) {
        winner = teamA.name();
        break;
      }
      if (teamA.isAllDead()) {
        winner = teamB.name();
        break;
      }
    }

    return buildResult(
        winner, round, teamA, teamB, initialHpA, initialHpB, damageDealt, skillProcs, combatLog);
  }

  // ===================== 回合处理 =====================

  void processOverTimeEffects(Team team, BuffManager buffManager) {
    for (Combatant c : team.aliveMembers()) {
      int effect = buffManager.processOverTimeEffects(c.getId());
      if (effect > 0) {
        c.heal(effect);
      } else if (effect < 0) {
        c.takeDamage(-effect);
      }
    }
  }

  // ===================== 技能选择 =====================

  List<Combatant> buildTurnOrder(Team teamA, Team teamB, BuffManager buffManager) {
    List<Combatant> all = new ArrayList<>();
    all.addAll(teamA.aliveMembers());
    all.addAll(teamB.aliveMembers());
    all.sort(
        (c1, c2) -> {
          int s1 = (int) (c1.getSpeed() * buffManager.getSpeedModifier(c1.getId()));
          int s2 = (int) (c2.getSpeed() * buffManager.getSpeedModifier(c2.getId()));
          return Integer.compare(s2, s1);
        });
    return all;
  }

  // ===================== 行动处理 =====================

  Skill selectSkill(Combatant attacker, Map<String, Integer> cooldowns, BuffManager buffManager) {
    boolean silenced = buffManager.hasSilence(attacker.getId());
    if (silenced) return null;

    List<Skill> skills = attacker.getSkills();
    if (skills == null || skills.isEmpty()) return null;

    List<Skill> available =
        skills.stream().filter(s -> !cooldowns.containsKey(skillKey(attacker, s))).toList();

    if (available.isEmpty()) return null;

    return available.get(ThreadLocalRandom.current().nextInt(available.size()));
  }

  // ===================== Buff 应用 =====================

  CombatLogEntry resolveAction(
      Combatant attacker,
      Combatant defender,
      Skill selectedSkill,
      Map<String, Integer> skillCooldowns,
      Map<String, Integer> skillProcs,
      Map<String, Integer> damageDealt,
      BuffManager buffManager,
      int round,
      int sequence) {
    int hpBefore = defender.getHp();
    int damage = 0;
    boolean isControl = false;
    boolean isBuff = false;
    String skillName = null;

    if (selectedSkill != null) {
      skillCooldowns.put(skillKey(attacker, selectedSkill), selectedSkill.getCooldownSeconds());
      skillProcs.merge(attacker.getName() + ":" + selectedSkill.getName(), 1, Integer::sum);
      skillName = selectedSkill.getName();

      List<SkillEffect> effects = selectedSkill.getEffects();
      if (effects != null && !effects.isEmpty()) {
        for (SkillEffect effect : effects) {
          double chance = effect.chance() != null ? effect.chance() : 1.0;
          if (Math.random() > chance) continue;

          switch (effect.type()) {
            case DAMAGE ->
                damage +=
                    damageCalculator.calculateEffectDamage(attacker, defender, effect, buffManager);
            case MULTI_HIT ->
                damage +=
                    damageCalculator.calculateEffectDamage(attacker, defender, effect, buffManager)
                        * 3;
            case ARMOR_BREAK -> {
              applyArmorBreak(defender, effect, selectedSkill, buffManager);
              isControl = true;
            }
            case SLOW -> {
              applySlow(defender, effect, selectedSkill, buffManager);
              isControl = true;
            }
            case DOT -> applyDot(attacker, defender, effect, selectedSkill, buffManager);
            case EXECUTE -> {
              int baseDmg =
                  damageCalculator.calculateEffectDamage(attacker, defender, effect, buffManager);
              double hpRatio = (double) defender.getHp() / defender.getMaxHp();
              double threshold = effect.value() != null ? effect.value() : 0.3;
              damage += (int) (baseDmg * (hpRatio < threshold ? 2.0 : 1.0));
            }
            case LIFESTEAL -> {
              int lifestealDmg =
                  damageCalculator.calculateNormalDamage(attacker, defender, buffManager);
              damage += lifestealDmg;
              double ratio = effect.value() != null ? effect.value() : 0.33;
              attacker.heal((int) (lifestealDmg * ratio));
            }
            case HEAL -> {
              double ratio = effect.value() != null ? effect.value() : 0.5;
              attacker.heal((int) (attacker.getAttack() * ratio));
              isBuff = true;
            }
            case ATTACK_BUFF -> {
              applyBuff(attacker, BuffType.ATTACK_BUFF, effect, selectedSkill, buffManager);
              isBuff = true;
            }
            case DEFENSE_BUFF -> {
              applyBuff(attacker, BuffType.DEFENSE_BUFF, effect, selectedSkill, buffManager);
              isBuff = true;
            }
            case SPEED_BUFF -> {
              applyBuff(attacker, BuffType.SPEED_BUFF, effect, selectedSkill, buffManager);
              isBuff = true;
            }
            case STUN, FREEZE, SILENCE -> {
              applyControlBuff(defender, effect.type(), effect, selectedSkill, buffManager);
              isControl = true;
            }
          }
        }
      }
      if (damage == 0 && !isBuff && !isControl) {
        damage = damageCalculator.calculateNormalDamage(attacker, defender, buffManager);
      }
    } else {
      damage = damageCalculator.calculateNormalDamage(attacker, defender, buffManager);
    }

    if (damage > 0) {
      defender.takeDamage(damage);
      damageDealt.merge(attacker.getName(), damage, Integer::sum);
    }

    List<String> effectNames =
        selectedSkill != null && selectedSkill.getEffects() != null
            ? selectedSkill.getEffects().stream().map(e -> e.type().name()).toList()
            : null;

    return new CombatLogEntry(
        round,
        sequence,
        attacker.getName(),
        isBuff ? attacker.getName() : defender.getName(),
        selectedSkill != null ? CombatLogEntry.AttackType.SKILL : CombatLogEntry.AttackType.NORMAL,
        skillName,
        effectNames,
        isControl || isBuff,
        isBuff ? attacker.getName() : null,
        damage,
        hpBefore,
        defender.getHp(),
        defender.getHp() <= 0);
  }

  private void applyArmorBreak(
      Combatant defender, SkillEffect effect, Skill skill, BuffManager buffManager) {
    double value = effect.value() != null ? effect.value() : 0.2;
    int duration = effect.duration() != null ? effect.duration() : 3;
    buffManager.addBuff(
        defender.getId(),
        Buff.builder()
            .type(BuffType.ARMOR_BREAK)
            .value(value)
            .remainingTurns(duration)
            .source(skill.getName())
            .build());
  }

  private void applySlow(
      Combatant defender, SkillEffect effect, Skill skill, BuffManager buffManager) {
    double value = effect.value() != null ? effect.value() : 0.3;
    int duration = effect.duration() != null ? effect.duration() : 2;
    buffManager.addBuff(
        defender.getId(),
        Buff.builder()
            .type(BuffType.SLOW)
            .value(value)
            .remainingTurns(duration)
            .source(skill.getName())
            .build());
  }

  private void applyDot(
      Combatant attacker,
      Combatant defender,
      SkillEffect effect,
      Skill skill,
      BuffManager buffManager) {
    double value = effect.value() != null ? effect.value() : 0.3;
    int duration = effect.duration() != null ? effect.duration() : 3;
    int maxStacks = effect.maxStacks() != null ? effect.maxStacks() : 3;
    buffManager.addBuff(
        defender.getId(),
        Buff.builder()
            .type(BuffType.DOT)
            .value(attacker.getAttack() * value)
            .remainingTurns(duration)
            .source(skill.getName())
            .stackable(true)
            .maxStacks(maxStacks)
            .build());
  }

  private void applyBuff(
      Combatant target, BuffType type, SkillEffect effect, Skill skill, BuffManager buffManager) {
    double value = effect.value() != null ? effect.value() : 0.2;
    int duration = effect.duration() != null ? effect.duration() : 3;
    buffManager.addBuff(
        target.getId(),
        Buff.builder()
            .type(type)
            .value(value)
            .remainingTurns(duration)
            .source(skill.getName())
            .build());
  }

  // ===================== 辅助方法 =====================

  private void applyControlBuff(
      Combatant defender,
      EffectType effectType,
      SkillEffect effect,
      Skill skill,
      BuffManager buffManager) {
    int duration = effect.duration() != null ? effect.duration() : 1;
    BuffType buffType =
        switch (effectType) {
          case FREEZE -> BuffType.FREEZE;
          case SILENCE -> BuffType.SILENCE;
          default -> BuffType.STUN;
        };
    buffManager.addBuff(
        defender.getId(),
        Buff.builder()
            .type(buffType)
            .value(1.0)
            .remainingTurns(duration)
            .source(skill.getName())
            .build());
  }

  private Combatant selectTarget(Team defenderTeam) {
    return defenderTeam.selectTargetForPVE();
  }

  void tickCooldowns(Map<String, Integer> skillCooldowns) {
    var it = skillCooldowns.entrySet().iterator();
    while (it.hasNext()) {
      var entry = it.next();
      int cd = entry.getValue() - 1;
      if (cd <= 0) {
        it.remove();
      } else {
        entry.setValue(cd);
      }
    }
  }

  Map<String, Integer> captureHp(Team team) {
    Map<String, Integer> hpMap = new LinkedHashMap<>();
    for (Combatant c : team.members()) {
      hpMap.put("member_" + c.getId(), c.getHp());
    }
    return hpMap;
  }

  private BattleResultVO buildResult(
      String winner,
      int round,
      Team teamA,
      Team teamB,
      Map<String, Integer> initialHpA,
      Map<String, Integer> initialHpB,
      Map<String, Integer> damageDealt,
      Map<String, Integer> skillProcs,
      List<CombatLogEntry> combatLog) {
    Map<String, Object> playerHpChange = new LinkedHashMap<>();
    for (Combatant c : teamA.members()) {
      Integer initial = initialHpA.get("member_" + c.getId());
      if (initial != null) {
        playerHpChange.put(c.getName(), Map.of("before", initial, "after", c.getHp()));
      }
    }

    List<Map<String, Object>> beastHpChanges = new ArrayList<>();
    for (Combatant c : teamA.members()) {
      if (c instanceof BeastCombatant) {
        Map<String, Object> change = new LinkedHashMap<>();
        Integer initial = initialHpA.get("member_" + c.getId());
        change.put("name", c.getName());
        change.put("before", initial != null ? initial : c.getHp());
        change.put("after", c.getHp());
        beastHpChanges.add(change);
      }
    }

    List<Map<String, Object>> monsterHpChanges = new ArrayList<>();
    for (Combatant c : teamB.members()) {
      Map<String, Object> change = new LinkedHashMap<>();
      change.put("name", c.getName());
      change.put("after", c.getHp());
      monsterHpChanges.add(change);
    }

    List<Map<String, Object>> damageList = new ArrayList<>();
    for (var entry : damageDealt.entrySet()) {
      damageList.add(Map.of("name", entry.getKey(), "total", entry.getValue()));
    }

    List<Map<String, Object>> skillProcList = new ArrayList<>();
    for (var entry : skillProcs.entrySet()) {
      skillProcList.add(Map.of("key", entry.getKey(), "count", entry.getValue()));
    }

    return BattleResultVO.builder()
        .winner(winner)
        .rounds(round)
        .playerHpChange(playerHpChange)
        .beastHpChanges(beastHpChanges)
        .monsterHpChanges(monsterHpChanges)
        .damageDealt(damageList)
        .skillProcs(skillProcList)
        .combatLog(combatLog)
        .build();
  }
}
