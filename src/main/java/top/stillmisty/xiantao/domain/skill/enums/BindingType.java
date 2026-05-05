package top.stillmisty.xiantao.domain.skill.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum BindingType {
  NONE("none", "无"),
  WEAPON_TYPE("weapon_type", "法器类型"),
  WEAPON_CATEGORY("weapon_category", "法器大类"),
  ELEMENT("element", "元素");

  @EnumValue private final String code;
  private final String name;

  BindingType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static BindingType fromCode(String code) {
    for (BindingType type : values()) {
      if (type.code.equalsIgnoreCase(code)) return type;
    }
    return NONE;
  }
}
