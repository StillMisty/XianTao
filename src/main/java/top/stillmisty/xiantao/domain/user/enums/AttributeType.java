package top.stillmisty.xiantao.domain.user.enums;

import lombok.Getter;

/** 属性类型枚举 */
@Getter
public enum AttributeType {
  STR("STR", "力道"),
  CON("CON", "根骨"),
  AGI("AGI", "身法"),
  WIS("WIS", "悟性");

  private final String code;
  private final String name;

  AttributeType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  /** 根据代码查找属性类型 */
  public static AttributeType fromCode(String code) {
    for (AttributeType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown AttributeType code: " + code);
  }
}
