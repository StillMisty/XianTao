package top.stillmisty.xiantao.domain.monster.enums;

import lombok.Getter;

/** Buff/Debuff类型枚举 */
@Getter
public enum BuffType {

  // Debuff（负面效果）
  ARMOR_BREAK("armor_break", "破甲", "降低防御力"),
  SLOW("slow", "减速", "降低速度"),
  DOT("dot", "持续伤害", "每回合造成伤害"),
  STUN("stun", "眩晕", "跳过行动"),
  FREEZE("freeze", "冰冻", "跳过行动+受伤增加"),
  SILENCE("silence", "沉默", "禁止技能"),

  // Buff（正面效果）
  HEAL("heal", "治疗", "恢复生命值"),
  ATTACK_BUFF("attack_buff", "攻击增益", "提升攻击力"),
  DEFENSE_BUFF("defense_buff", "防御增益", "提升防御力"),
  SPEED_BUFF("speed_buff", "速度增益", "提升速度");

  private final String code;
  private final String name;
  private final String description;

  BuffType(String code, String name, String description) {
    this.code = code;
    this.name = name;
    this.description = description;
  }
}
