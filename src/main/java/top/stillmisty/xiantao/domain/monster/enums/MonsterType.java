package top.stillmisty.xiantao.domain.monster.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 怪物类型枚举 */
@Getter
public enum MonsterType {
  BEAST("BEAST", "妖兽"),
  SPIRIT("SPIRIT", "灵体"),
  ARMORED("ARMORED", "甲胄"),
  WILD_BEAST("WILD_BEAST", "猛兽"),
  EVIL("EVIL", "邪祟"),
  FLYING("FLYING", "飞行"),
  HUMAN("HUMAN", "人形");

  @EnumValue private final String code;
  private final String name;

  MonsterType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static MonsterType fromCode(String code) {
    for (MonsterType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown MonsterType code: " + code);
  }
}
