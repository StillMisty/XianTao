package top.stillmisty.xiantao.service.combat;

import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.BuffManager;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
import top.stillmisty.xiantao.domain.monster.enums.BuffType;

/** 受击反应层 — 冰冻易伤、闪避、反伤、反击等防御侧规则的唯一归属，供战斗引擎在伤害落地时调用。 */
@Component
@RequiredArgsConstructor
public class ReactiveEffectProcessor {

  private static final double FROZEN_VULNERABILITY = 1.3;

  private final DamageCalculator damageCalculator;

  /** 单次伤害落地结果 */
  public record AppliedDamage(int damage, boolean dodged) {}

  /**
   * 结算一次攻击对防守方的最终影响：冰冻易伤放大 → 闪避判定 → 扣血 → 反伤 → 反击。
   *
   * @param incomingDamage 攻击方打出的原始伤害（已含技能/普攻计算）
   * @return 实际记入战斗日志的伤害（闪避时为 0）与是否被闪避
   */
  public AppliedDamage apply(
      Combatant attacker, Combatant defender, int incomingDamage, BuffManager buffManager) {
    int damage = incomingDamage;

    // 冰冻目标受到的伤害放大
    if (buffManager.getBuffsByType(defender.getId(), BuffType.FREEZE).stream()
        .anyMatch(b -> !b.isExpired())) {
      damage = (int) (damage * FROZEN_VULNERABILITY);
    }

    // 被动抗性（RESIST_BUFF）：按比例削减所受伤害，上限 90%
    if (defender instanceof PlayerCombatant player && player.getPassiveResistPercent() > 0) {
      double resist = Math.min(0.9, player.getPassiveResistPercent());
      damage = Math.max(1, (int) Math.round(damage * (1 - resist)));
    }

    // 闪避判定：闪避成功则本次攻击完全落空（被动法决闪避与战斗增益叠加）
    double dodgeChance = buffManager.getDodgeChance(defender.getId());
    if (defender instanceof PlayerCombatant player) {
      dodgeChance = Math.min(1.0, dodgeChance + player.getPassiveDodgePercent());
    }
    if (ThreadLocalRandom.current().nextDouble() < dodgeChance) {
      return new AppliedDamage(0, true);
    }

    defender.takeDamage(damage);

    applyReflect(attacker, defender, damage, buffManager);
    applyCounter(attacker, defender, buffManager);

    return new AppliedDamage(damage, false);
  }

  /** 反伤：按反弹比例将所受伤害返还攻击者（攻击者存活时生效） */
  private void applyReflect(
      Combatant attacker, Combatant defender, int damage, BuffManager buffManager) {
    double reflectPercent = buffManager.getReflectPercent(defender.getId());
    if (reflectPercent > 0 && attacker.isAlive()) {
      int reflectDamage = Math.max(1, (int) Math.round(damage * reflectPercent));
      attacker.takeDamage(reflectDamage);
    }
  }

  /** 反击：概率对攻击者追加一次普攻伤害 */
  private void applyCounter(Combatant attacker, Combatant defender, BuffManager buffManager) {
    if (attacker.isAlive()
        && ThreadLocalRandom.current().nextDouble()
            < buffManager.getCounterChance(defender.getId())) {
      int counterDamage =
          Math.max(
              1,
              damageCalculator.calculateNormalDamage(
                  /* attacker= */ defender, /* defender= */ attacker, buffManager));
      attacker.takeDamage(counterDamage);
    }
  }
}
