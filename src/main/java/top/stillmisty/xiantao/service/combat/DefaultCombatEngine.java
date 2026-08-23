package top.stillmisty.xiantao.service.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.BattleContext;
import top.stillmisty.xiantao.domain.monster.BuffManager;
import top.stillmisty.xiantao.domain.monster.CombatEngine;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.SkillSelectionStrategy;
import top.stillmisty.xiantao.domain.monster.TargetSelectionStrategy;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;
import top.stillmisty.xiantao.domain.monster.vo.BattleResultVO;
import top.stillmisty.xiantao.domain.monster.vo.CombatLogEntry;
import top.stillmisty.xiantao.domain.monster.vo.HpChange;
import top.stillmisty.xiantao.domain.monster.vo.SkillProc;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultCombatEngine implements CombatEngine {

  private final DamageCalculator damageCalculator;
  private final EffectHandlerRegistry effectHandlerRegistry;
  private final SkillSelectionStrategy skillSelectionStrategy;
  private final TargetSelectionStrategy targetSelectionStrategy;

  private static String skillKey(Combatant attacker, Skill skill) {
    return attacker.getId() + ":" + skill.getId();
  }

  @Override
  public BattleResultVO simulate(BattleContext context) {
    CombatTeam teamA = context.getTeamA();
    CombatTeam teamB = context.getTeamB();
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

        CombatTeam attackerTeam = teamA.members().contains(attacker) ? teamA : teamB;
        CombatTeam defenderTeam = attackerTeam.equals(teamA) ? teamB : teamA;
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
                sequence,
                defenderTeam);
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

  void processOverTimeEffects(CombatTeam team, BuffManager buffManager) {
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

  List<Combatant> buildTurnOrder(CombatTeam teamA, CombatTeam teamB, BuffManager buffManager) {
    List<Combatant> all = new ArrayList<>();
    all.addAll(teamA.aliveMembers());
    all.addAll(teamB.aliveMembers());
    all.sort(
        (c1, c2) -> {
          double s1 = c1.getSpeed() * buffManager.getSpeedModifier(c1.getId());
          double s2 = c2.getSpeed() * buffManager.getSpeedModifier(c2.getId());
          return Double.compare(s2, s1);
        });
    return all;
  }

  // ===================== 行动处理 =====================

  @Nullable Skill selectSkill(
      Combatant attacker, Map<String, Integer> cooldowns, BuffManager buffManager) {
    boolean silenced = buffManager.hasSilence(attacker.getId());
    if (silenced) return null;

    List<Skill> skills = attacker.getSkills();
    if (skills == null || skills.isEmpty()) return null;

    List<Skill> available =
        skills.stream().filter(s -> !cooldowns.containsKey(skillKey(attacker, s))).toList();

    return skillSelectionStrategy.selectSkill(attacker, available);
  }

  // ===================== 效果处理 =====================

  CombatLogEntry resolveAction(
      Combatant attacker,
      Combatant defender,
      @Nullable Skill selectedSkill,
      Map<String, Integer> skillCooldowns,
      Map<String, Integer> skillProcs,
      Map<String, Integer> damageDealt,
      BuffManager buffManager,
      int round,
      int sequence,
      CombatTeam defenderTeam) {
    int hpBefore = defender.getHp();
    int damage = 0;
    boolean isControl = false;
    boolean isBuff = false;
    boolean dodged = false;
    String skillName = null;

    if (selectedSkill != null) {
      double attackSpeed = Math.max(0.01, attacker.getAttackSpeed());
      Integer cdSeconds = selectedSkill.getCooldownSeconds();
      int cooldownTicks = cdSeconds != null ? (int) Math.max(1, cdSeconds / attackSpeed) : 1;

      List<SkillEffect> effects = selectedSkill.getEffects();
      boolean anyEffectProcessed = false;
      if (effects != null && !effects.isEmpty()) {
        for (SkillEffect effect : effects) {
          double chance = effect.chance() != null ? effect.chance() : 1.0;
          if (ThreadLocalRandom.current().nextDouble() > chance) continue;

          EffectHandler handler = effectHandlerRegistry.getHandler(effect.type());
          if (handler == null) continue;

          var ctx =
              new EffectHandler.EffectContext(
                  attacker,
                  defender,
                  effect,
                  selectedSkill,
                  buffManager,
                  defenderTeam,
                  damageCalculator);
          var result = handler.handle(ctx);
          damage += result.damage();
          isControl = isControl || result.isControl();
          isBuff = isBuff || result.isBuff();
          anyEffectProcessed =
              anyEffectProcessed || result.damage() > 0 || result.isControl() || result.isBuff();
        }
      }

      // 技能生效才计入冷却与统计；全部效果未命中时退化为普通攻击，不空耗 CD
      if (anyEffectProcessed) {
        skillCooldowns.put(skillKey(attacker, selectedSkill), cooldownTicks);
        skillProcs.merge(attacker.getName() + ":" + selectedSkill.getName(), 1, Integer::sum);
        skillName = selectedSkill.getName();
      } else {
        selectedSkill = null;
        damage = damageCalculator.calculateNormalDamage(attacker, defender, buffManager);
      }
    } else {
      damage = damageCalculator.calculateNormalDamage(attacker, defender, buffManager);
    }

    if (damage > 0) {
      if (buffManager.getBuffsByType(defender.getId(), BuffType.FREEZE).stream()
          .anyMatch(b -> !b.isExpired())) {
        damage = (int) (damage * 1.3);
      }

      // 闪避判定：闪避成功则本次攻击完全落空
      if (ThreadLocalRandom.current().nextDouble() < buffManager.getDodgeChance(defender.getId())) {
        damage = 0;
        dodged = true;
      } else {
        defender.takeDamage(damage);
        damageDealt.merge(attacker.getName(), damage, Integer::sum);

        // 反伤：按反弹比例将所受伤害返还攻击者（攻击者存活时生效）
        double reflectPercent = buffManager.getReflectPercent(defender.getId());
        if (reflectPercent > 0 && attacker.isAlive()) {
          int reflectDamage = Math.max(1, (int) Math.round(damage * reflectPercent));
          attacker.takeDamage(reflectDamage);
        }

        // 反击：概率对攻击者追加一次普攻伤害
        if (attacker.isAlive()
            && ThreadLocalRandom.current().nextDouble()
                < buffManager.getCounterChance(defender.getId())) {
          int counterDamage =
              Math.max(1, damageCalculator.calculateNormalDamage(defender, attacker, buffManager));
          attacker.takeDamage(counterDamage);
        }
      }
    }

    List<String> effectNames =
        selectedSkill != null && selectedSkill.getEffects() != null
            ? new ArrayList<>(
                selectedSkill.getEffects().stream().map(e -> e.type().name()).toList())
            : new ArrayList<>();
    if (dodged) {
      effectNames.add("DODGED");
    }

    return new CombatLogEntry(
        round,
        sequence,
        attacker.getName(),
        isBuff ? attacker.getName() : defender.getName(),
        selectedSkill != null ? CombatLogEntry.AttackType.SKILL : CombatLogEntry.AttackType.NORMAL,
        skillName,
        effectNames.isEmpty() ? null : List.copyOf(effectNames),
        isControl || isBuff,
        isBuff ? attacker.getName() : null,
        damage,
        hpBefore,
        defender.getHp(),
        defender.getHp() <= 0);
  }

  // ===================== 辅助方法 =====================

  @Nullable
  private Combatant selectTarget(CombatTeam defenderTeam) {
    return targetSelectionStrategy.selectTarget(defenderTeam);
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

  Map<String, Integer> captureHp(CombatTeam team) {
    Map<String, Integer> hpMap = new LinkedHashMap<>();
    for (Combatant c : team.members()) {
      hpMap.put("member_" + c.getId(), c.getHp());
    }
    return hpMap;
  }

  private BattleResultVO buildResult(
      String winner,
      int round,
      CombatTeam teamA,
      CombatTeam teamB,
      Map<String, Integer> initialHpA,
      Map<String, Integer> initialHpB,
      Map<String, Integer> damageDealt,
      Map<String, Integer> skillProcs,
      List<CombatLogEntry> combatLog) {
    Map<String, HpChange> playerHpChange = new LinkedHashMap<>();
    for (Combatant c : teamA.members()) {
      Integer initial = initialHpA.get("member_" + c.getId());
      if (initial != null) {
        playerHpChange.put(c.getName(), new HpChange(initial, c.getHp()));
      }
    }

    List<SkillProc> skillProcList = new ArrayList<>();
    for (var entry : skillProcs.entrySet()) {
      skillProcList.add(new SkillProc(entry.getKey(), entry.getValue()));
    }

    return BattleResultVO.builder()
        .winner(winner)
        .rounds(round)
        .playerHpChange(playerHpChange)
        .skillProcs(skillProcList)
        .combatLog(combatLog)
        .build();
  }
}
