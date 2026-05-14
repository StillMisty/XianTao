package top.stillmisty.xiantao.domain.dungeon.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PoiType {
  GATHER("GATHER", "采集"),
  COMBAT("COMBAT", "战斗"),
  SEARCH("SEARCH", "搜索"),
  BOSS("BOSS", "首领");

  @EnumValue private final String code;
  private final String name;

  PoiType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static PoiType fromCode(String code) {
    for (PoiType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown PoiType code: " + code);
  }
}
