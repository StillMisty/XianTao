package top.stillmisty.xiantao.domain.item.enums;

import lombok.Getter;

/** 锻材三性属性枚举 */
@Getter
public enum MaterialAttribute {
  RIGIDITY("RIGIDITY", "刚性"),
  TOUGHNESS("TOUGHNESS", "韧性"),
  SPIRIT("SPIRIT", "灵性");

  private final String code;
  private final String name;

  MaterialAttribute(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static MaterialAttribute fromCode(String code) {
    for (MaterialAttribute attr : values()) {
      if (attr.code.equals(code)) {
        return attr;
      }
    }
    throw new IllegalArgumentException("Unknown MaterialAttribute code: " + code);
  }
}
