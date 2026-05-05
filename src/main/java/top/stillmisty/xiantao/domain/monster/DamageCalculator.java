package top.stillmisty.xiantao.domain.monster;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;

@Slf4j
@Component
public class DamageCalculator {

  private static final Map<WeaponType, MonsterType> ADVANTAGE_MAP =
      Map.of(
          WeaponType.BLADE, MonsterType.BEAST,
          WeaponType.SWORD, MonsterType.SPIRIT,
          WeaponType.AXE, MonsterType.ARMORED,
          WeaponType.SPEAR, MonsterType.WILD_BEAST,
          WeaponType.STAFF, MonsterType.EVIL,
          WeaponType.BOW, MonsterType.FLYING);

  public int calculateNormalDamage(
      Combatant attacker, Combatant defender, BuffManager buffManager) {
    double advantageMultiplier = getAdvantageMultiplier(attacker, defender);

    double attackModifier = buffManager.getAttackModifier(attacker.getId());
    int rawDamage = (int) Math.round(attacker.getAttack() * advantageMultiplier * attackModifier);

    int reduction = calculateReduction(defender, buffManager);
    return Math.max(1, rawDamage - reduction);
  }

  public int calculateEffectDamage(
      Combatant attacker, Combatant defender, SkillEffect effect, BuffManager buffManager) {
    String formula = effect.formula();
    double advantageMultiplier = getAdvantageMultiplier(attacker, defender);
    double attackModifier = buffManager.getAttackModifier(attacker.getId());

    int baseDmg;
    if (formula != null && !formula.isBlank()) {
      if (attacker instanceof PlayerCombatant pc) {
        baseDmg = evaluateFormula(formula, pc.getWis());
      } else {
        baseDmg = (int) Math.round(attacker.getAttack() * 1.5);
      }
    } else {
      baseDmg = attacker.getAttack();
    }

    int rawDamage = (int) Math.round(baseDmg * advantageMultiplier * attackModifier);
    int reduction = calculateReduction(defender, buffManager);
    return Math.max(1, rawDamage - reduction);
  }

  public int calculateReduction(Combatant defender, BuffManager buffManager) {
    double defenseModifier = buffManager.getDefenseModifier(defender.getId());
    double defenseBonus = buffManager.getDefenseBonusModifier(defender.getId());
    return (int) Math.round(defender.getDefense() * 0.4 * defenseModifier * defenseBonus);
  }

  public double getAdvantageMultiplier(Combatant attacker, Combatant defender) {
    if (attacker instanceof PlayerCombatant pc && defender instanceof Monster monster) {
      WeaponType weaponType = pc.getWeaponType();
      MonsterType monsterType = monster.getMonsterType();
      if (weaponType != null && monsterType != null) {
        MonsterType advantaged = ADVANTAGE_MAP.get(weaponType);
        if (advantaged == monsterType) return 1.5;
      }
    }
    return 1.0;
  }

  public int evaluateFormula(String formula, int wis) {
    try {
      String expr = formula.replace("wis", String.valueOf(wis)).replaceAll("\\s+", "");
      return evaluateExpression(expr);
    } catch (Exception e) {
      log.warn("公式计算失败: formula={}, wis={}", formula, wis, e);
      return 10;
    }
  }

  int evaluateExpression(String expr) {
    expr = expr.replaceAll("(?<=\\d)-", "+-");
    if (expr.startsWith("+")) {
      expr = expr.substring(1);
    }
    int result = 0;
    for (String part : expr.split("\\+")) {
      part = part.trim();
      if (part.isEmpty()) continue;
      result += evaluatePart(part);
    }
    return result;
  }

  private int evaluatePart(String part) {
    if (part.contains("*")) {
      int product = 1;
      for (String mp : part.split("\\*")) {
        product *= Integer.parseInt(mp.trim());
      }
      return product;
    }
    return Integer.parseInt(part.trim());
  }
}
