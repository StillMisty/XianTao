package top.stillmisty.xiantao.domain.shop.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ProductType {
  ITEM("ITEM", "物品"),
  EQUIPMENT("EQUIPMENT", "装备");

  @EnumValue private final String code;

  private final String name;

  ProductType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static ProductType fromCode(String code) {
    for (ProductType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ProductType code: " + code);
  }
}
