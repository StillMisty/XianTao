package top.stillmisty.xiantao.domain.skill.enums;

import lombok.Getter;

@Getter
public enum EffectType {
  DAMAGE("DAMAGE", "伤害"),
  ARMOR_BREAK("ARMOR_BREAK", "破甲"),
  SLOW("SLOW", "减速"),
  EXECUTE("EXECUTE", "斩杀"),
  LIFESTEAL("LIFESTEAL", "吸血"),
  MULTI_HIT("MULTI_HIT", "连击"),
  DOT("DOT", "持续伤害"),
  HEAL("HEAL", "治疗"),
  ATTACK_BUFF("ATTACK_BUFF", "攻击增益"),
  DEFENSE_BUFF("DEFENSE_BUFF", "防御增益"),
  SPEED_BUFF("SPEED_BUFF", "速度增益"),
  STUN("STUN", "眩晕"),
  FREEZE("FREEZE", "冰冻"),
  SILENCE("SILENCE", "沉默"),
  AOE_DAMAGE("AOE_DAMAGE", "群体伤害");

  private final String code;
  private final String name;

  EffectType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static EffectType fromCode(String code) {
    for (EffectType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown EffectType code: " + code);
  }
}
