package top.stillmisty.xiantao.domain.sect.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 对话类型枚举 */
@Getter
public enum ChatType {
  SPIRIT("SPIRIT", "地灵"),
  SHOP("SHOP", "商铺"),
  SECT("SECT", "宗灵"),
  TRAVELER("TRAVELER", "旅行商人");

  @EnumValue private final String code;
  private final String name;

  ChatType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static ChatType fromCode(String code) {
    for (ChatType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未知的对话类型: " + code);
  }
}
