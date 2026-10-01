package top.stillmisty.xiantao.domain.pill.enums;

import lombok.Getter;

/** 五行属性枚举 */
@Getter
public enum ElementType {
  METAL("METAL", "金"),
  WOOD("WOOD", "木"),
  WATER("WATER", "水"),
  FIRE("FIRE", "火"),
  EARTH("EARTH", "土");

  private final String code;
  private final String name;

  ElementType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static ElementType fromCode(String code) {
    for (ElementType type : values()) {
      if (type.code.equalsIgnoreCase(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ElementType code: " + code);
  }

  /** 容错显示名：未知编码原样返回（丹方数据里的键为小写，如 metal）。 */
  public static String displayName(String code) {
    for (ElementType type : values()) {
      if (type.code.equalsIgnoreCase(code)) {
        return type.name;
      }
    }
    return code;
  }
}
