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
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ElementType code: " + code);
  }
}
