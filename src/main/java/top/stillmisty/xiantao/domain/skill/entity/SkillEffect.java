package top.stillmisty.xiantao.domain.skill.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;

/** 法决效果定义 存储在 Skill.effects JSONB 列表中 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SkillEffect(
    /*
     效果类型
    */
    EffectType type,

    /*
     伤害公式（仅 type=damage/multi_hit/execute 时使用）
    */
    String formula,

    /*
     效果数值
     - 对于 buff/debuff：百分比（如 0.3 表示 30%）
     - 对于 dot：每回合伤害比例
     - 对于 heal：治疗比例
    */
    Double value,

    /*
     持续回合数（buff/debuff/dot 使用）
    */
    Integer duration,

    /*
     最大叠加层数（dot 使用）
    */
    Integer maxStacks,

    /*
     触发概率（0-1，默认 1.0）
    */
    Double chance,

    /*
     元素类型（可选，如 fire/ice/lightning）
    */
    String element,

    /*
     目标选择（可选：single/aoe/random）
    */
    String target) {
  /** 创建伤害效果 */
  public static SkillEffect damage(String formula) {
    return new SkillEffect(EffectType.DAMAGE, formula, null, null, null, null, null, null);
  }

  /** 创建多段伤害效果 */
  public static SkillEffect multiHit(String formula) {
    return new SkillEffect(EffectType.MULTI_HIT, formula, null, null, null, null, null, null);
  }

  /** 创建破甲效果 */
  public static SkillEffect armorBreak(double value, int duration) {
    return new SkillEffect(EffectType.ARMOR_BREAK, null, value, duration, null, null, null, null);
  }

  /** 创建减速效果 */
  public static SkillEffect slow(double value, int duration) {
    return new SkillEffect(EffectType.SLOW, null, value, duration, null, null, null, null);
  }

  /** 创建持续伤害效果 */
  public static SkillEffect dot(double value, int duration, int maxStacks) {
    return new SkillEffect(EffectType.DOT, null, value, duration, maxStacks, null, null, null);
  }

  /** 创建斩杀效果 */
  public static SkillEffect execute(String formula, double hpThreshold) {
    return new SkillEffect(EffectType.EXECUTE, formula, hpThreshold, null, null, null, null, null);
  }

  /** 创建吸血效果 */
  public static SkillEffect lifesteal(double ratio) {
    return new SkillEffect(EffectType.LIFESTEAL, null, ratio, null, null, null, null, null);
  }

  /** 创建治疗效果 */
  public static SkillEffect heal(double ratio) {
    return new SkillEffect(EffectType.HEAL, null, ratio, null, null, null, null, null);
  }

  /** 创建攻击增益效果 */
  public static SkillEffect attackBuff(double value, int duration) {
    return new SkillEffect(EffectType.ATTACK_BUFF, null, value, duration, null, null, null, null);
  }

  /** 创建防御增益效果 */
  public static SkillEffect defenseBuff(double value, int duration) {
    return new SkillEffect(EffectType.DEFENSE_BUFF, null, value, duration, null, null, null, null);
  }

  /** 创建速度增益效果 */
  public static SkillEffect speedBuff(double value, int duration) {
    return new SkillEffect(EffectType.SPEED_BUFF, null, value, duration, null, null, null, null);
  }
}
