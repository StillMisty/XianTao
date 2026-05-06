package top.stillmisty.xiantao.domain.skill.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SkillType {
  ACTIVE("ACTIVE", "主动"),
  PASSIVE("PASSIVE", "被动");

  @EnumValue private final String code;
  private final String name;

  SkillType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static SkillType fromCode(String code) {
    for (SkillType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown SkillType code: " + code);
  }
}
