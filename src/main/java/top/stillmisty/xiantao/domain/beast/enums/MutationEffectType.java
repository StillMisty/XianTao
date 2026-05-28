package top.stillmisty.xiantao.domain.beast.enums;

import lombok.Getter;

/** 变异效果类型枚举 */
@Getter
public enum MutationEffectType {
  // 数值增益类
  ATTACK_PERCENT("ATTACK_PERCENT", "攻击力百分比提升"),
  DEFENSE_PERCENT("DEFENSE_PERCENT", "防御力百分比提升"),
  SPEED_PERCENT("SPEED_PERCENT", "速度百分比提升"),
  EXP_PERCENT("EXP_PERCENT", "修为获取百分比提升"),
  OUTPUT_PERCENT("OUTPUT_PERCENT", "产出量百分比提升"),
  OUTPUT_INTERVAL_REDUCE("OUTPUT_INTERVAL_REDUCE", "产出间隔减少"),
  QUALITY_UP_CHANCE("QUALITY_UP_CHANCE", "品质突破概率提升"),
  RARE_ITEM_CHANCE("RARE_ITEM_CHANCE", "稀有物品概率"),

  // 繁育类
  BREED_QUALITY_BOOST("BREED_QUALITY_BOOST", "繁育品质提升"),
  BREED_COOLDOWN_REDUCE("BREED_COOLDOWN_REDUCE", "繁育冷却减少"),
  INHERIT_RATE_BOOST("INHERIT_RATE_BOOST", "词条继承率提升"),

  // 战斗触发类
  ON_BATTLE_END_HEAL("ON_BATTLE_END_HEAL", "战斗结束回血"),
  COUNTER_ATTACK("COUNTER_ATTACK", "受击反击"),
  DAMAGE_REDUCE_CHANCE("DAMAGE_REDUCE_CHANCE", "概率减伤"),
  LIFESTEAL_PERCENT("LIFESTEAL_PERCENT", "吸血比例"),
  LOW_HP_ATTACK_BOOST("LOW_HP_ATTACK_BOOST", "低血量攻击加成"),
  CRITICAL_CHANCE("CRITICAL_CHANCE", "暴击概率"),
  ARMOR_PENETRATE_PERCENT("ARMOR_PENETRATE_PERCENT", "无视防御比例"),
  PHYSICAL_DAMAGE_REDUCE("PHYSICAL_DAMAGE_REDUCE", "物理伤害减免"),
  IMMUNITY_CHANCE("IMMUNITY_CHANCE", "免疫伤害概率"),
  DODGE_CHANCE("DODGE_CHANCE", "闪避概率"),
  FIRST_ATTACK_SPEED_BOOST("FIRST_ATTACK_SPEED_BOOST", "首次攻击速度加成"),
  HP_REGEN_PERCENT("HP_REGEN_PERCENT", "每回合回血比例"),
  MANA_GAIN("MANA_GAIN", "灵力获取"),
  REVIVE_CHANCE("REVIVE_CHANCE", "复活概率"),
  AOE_CHANCE("AOE_CHANCE", "范围伤害概率"),
  FIRST_ATTACK_DODGE("FIRST_ATTACK_DODGE", "首次攻击闪避"),
  DAMAGE_TRANSFER("DAMAGE_TRANSFER", "伤害转移概率"),
  SKIP_TURN_CHANCE("SKIP_TURN_CHANCE", "跳过回合概率"),
  ALL_STATS_PERCENT("ALL_STATS_PERCENT", "全属性提升"),
  EXP_REDUCE_PERCENT("EXP_REDUCE_PERCENT", "升级修为减少"),
  TEMPORARY_DEFENSE_BOOST("TEMPORARY_DEFENSE_BOOST", "临时防御加成"),
  STACKING_ATTACK_BOOST("STACKING_ATTACK_BOOST", "叠加攻击加成"),
  IGNORE_DODGE_CHANCE("IGNORE_DODGE_CHANCE", "无视闪避概率"),
  STUN_CHANCE("STUN_CHANCE", "眩晕概率"),
  MANA_REGEN("MANA_REGEN", "每回合灵力恢复"),
  PROBABILITY_MULTIPLIER("PROBABILITY_MULTIPLIER", "概率效果增幅"),
  EVADE_CHANCE("EVADE_CHANCE", "完全闪避概率"),
  SURVIVE_LETHAL_CHANCE("SURVIVE_LETHAL_CHANCE", "致命伤害存活概率"),
  RARE_OFFSPRING_CHANCE("RARE_OFFSPRING_CHANCE", "稀有后代概率"),
  TWIN_CHANCE("TWIN_CHANCE", "双生概率"),
  INHERIT_EXISTING_TRAIT("INHERIT_EXISTING_TRAIT", "继承已有词条"),
  EXP_PERCENT_STRONG_ENEMY("EXP_PERCENT_STRONG_ENEMY", "强敌修为加成");

  private final String code;
  private final String name;

  MutationEffectType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static MutationEffectType fromCode(String code) {
    for (MutationEffectType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown MutationEffectType code: " + code);
  }
}
