package top.stillmisty.xiantao.domain.dungeon.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DungeonElementType {
  METAL("METAL", "金"),
  WOOD("WOOD", "木"),
  WATER("WATER", "水"),
  FIRE("FIRE", "火"),
  EARTH("EARTH", "土");

  @EnumValue private final String code;
  private final String name;

  DungeonElementType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static DungeonElementType fromCode(String code) {
    for (DungeonElementType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown DungeonElementType code: " + code);
  }
}
