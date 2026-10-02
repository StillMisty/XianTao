package top.stillmisty.xiantao.domain.skill.vo;

import java.util.List;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;

/**
 * 已习得被动法决的恒定加成聚合（习得即生效，不占战斗槽位）。
 *
 * <p>数值口径与折算规则见 {@link #aggregate} 处的注释。
 */
public record PassiveSkillBonuses(
    double attackPercent,
    double defensePercent,
    double speedPercent,
    double dodgePercent,
    double resistPercent,
    double hpPercent,
    double surviveLethalPercent) {

  public static final PassiveSkillBonuses NONE = new PassiveSkillBonuses(0, 0, 0, 0, 0, 0, 0);

  // 种子数据中被动效果 value 为百分数（如 10 = 10%），聚合时折算为小数（0.10）。
  // 带 duration 的效果属于主动施放的临时效果，不参与恒定加成；
  // SURVIVE_LETHAL / HEAL（无公式）折算为「每场战斗一次濒死免死，保留 value% 气血」。
  public static PassiveSkillBonuses aggregate(List<Skill> skills) {
    double attack = 0;
    double defense = 0;
    double speed = 0;
    double dodge = 0;
    double resist = 0;
    double hp = 0;
    double surviveLethal = 0;

    for (Skill skill : skills) {
      if (!skill.isPassive()) continue;
      List<SkillEffect> effects = skill.getEffects();
      if (effects == null) continue;
      for (SkillEffect effect : effects) {
        if (effect == null || effect.value() == null) continue;
        if (effect.duration() != null) continue;
        double percent = effect.value() / 100.0;
        switch (effect.type()) {
          case ATTACK_BUFF -> attack += percent;
          case DEFENSE_BUFF -> defense += percent;
          case SPEED_BUFF -> speed += percent;
          case DODGE -> dodge += percent;
          case RESIST_BUFF -> resist += percent;
          case HP_BUFF -> hp += percent;
          case SURVIVE_LETHAL, HEAL -> surviveLethal = Math.max(surviveLethal, percent);
          default -> {}
        }
      }
    }
    return new PassiveSkillBonuses(attack, defense, speed, dodge, resist, hp, surviveLethal);
  }
}
