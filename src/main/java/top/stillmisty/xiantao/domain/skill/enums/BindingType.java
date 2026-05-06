package top.stillmisty.xiantao.domain.skill.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum BindingType {
  NONE("NONE", "无"),
  WEAPON_TYPE("WEAPON_TYPE", "法器类型"),
  WEAPON_CATEGORY("WEAPON_CATEGORY", "法器大类"),
  ELEMENT("ELEMENT", "元素");

  @EnumValue private final String code;
  private final String name;

  BindingType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static BindingType fromCode(String code) {
    for (BindingType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown BindingType code: " + code);
  }
}
