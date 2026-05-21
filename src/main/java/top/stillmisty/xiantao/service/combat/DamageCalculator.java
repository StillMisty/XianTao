package top.stillmisty.xiantao.service.combat;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.enums.WeaponType;
import top.stillmisty.xiantao.domain.monster.BuffManager;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.Monster;
import top.stillmisty.xiantao.domain.monster.PlayerCombatant;
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
        baseDmg = evaluateFormula(formula, pc);
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

  /** 获取属性克制倍率。仅玩家对怪物生效；灵兽不使用武器，不参与属性克制。 */
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

  public int evaluateFormula(String formula, PlayerCombatant pc) {
    try {
      String expr =
          formula
              .replaceAll("\\battack\\b", String.valueOf(pc.getAttack()))
              .replaceAll("\\bwis\\b", String.valueOf(pc.getWis()))
              .replaceAll("\\bstr\\b", String.valueOf(pc.getStr()))
              .replaceAll("\\bagi\\b", String.valueOf(pc.getAgi()))
              .replaceAll("\\batk\\b", String.valueOf(pc.getAttack()))
              .replaceAll("\\s+", "");
      return evaluateExpression(expr);
    } catch (Exception e) {
      log.warn(
          "公式计算失败: formula={}, wis={}, str={}, agi={}, atk={}",
          formula,
          pc.getWis(),
          pc.getStr(),
          pc.getAgi(),
          pc.getAttack(),
          e);
      return 10;
    }
  }

  int evaluateExpression(String expr) {
    return (int) Math.clamp(new ExprParser(expr).parse(), 0, Integer.MAX_VALUE);
  }

  /** 递归下降表达式解析器，支持 +, -, *, /, 括号和运算符优先级，使用 long 防止溢出 */
  private static class ExprParser {
    private final String input;
    private int pos;

    ExprParser(String input) {
      this.input = input;
      this.pos = 0;
    }

    long parse() {
      long result = parseAddSub();
      if (pos < input.length()) {
        throw new IllegalArgumentException("Unexpected character: " + input.charAt(pos));
      }
      return result;
    }

    private long parseAddSub() {
      long left = parseMulDiv();
      while (pos < input.length()) {
        char op = input.charAt(pos);
        if (op == '+') {
          pos++;
          left += parseMulDiv();
        } else if (op == '-') {
          pos++;
          left -= parseMulDiv();
        } else {
          break;
        }
      }
      return left;
    }

    private long parseMulDiv() {
      long left = parseFactor();
      while (pos < input.length()) {
        char op = input.charAt(pos);
        if (op == '*') {
          pos++;
          left *= parseFactor();
        } else if (op == '/') {
          pos++;
          long divisor = parseFactor();
          if (divisor == 0) throw new ArithmeticException("Division by zero");
          left /= divisor;
        } else {
          break;
        }
      }
      return left;
    }

    private long parseFactor() {
      if (pos >= input.length()) {
        throw new IllegalArgumentException("Unexpected end of expression");
      }
      char c = input.charAt(pos);
      if (c == '(') {
        pos++;
        long val = parseAddSub();
        if (pos >= input.length() || input.charAt(pos) != ')') {
          throw new IllegalArgumentException("Missing closing parenthesis");
        }
        pos++;
        return val;
      }
      int start = pos;
      while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
        pos++;
      }
      if (start == pos) {
        throw new IllegalArgumentException("Expected number at position " + pos);
      }
      return Long.parseLong(input.substring(start, pos));
    }
  }
}
