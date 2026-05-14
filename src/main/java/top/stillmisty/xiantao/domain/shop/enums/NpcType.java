package top.stillmisty.xiantao.domain.shop.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NpcType {
  FUDI_SPIRIT("FUDI_SPIRIT", "地灵"),
  SHOP_KEEPER("SHOP_KEEPER", "商铺掌柜"),
  TRAVELER("TRAVELER", "旅行商人");

  @EnumValue private final String code;

  private final String name;

  NpcType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static NpcType fromCode(String code) {
    for (NpcType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown NpcType code: " + code);
  }
}
