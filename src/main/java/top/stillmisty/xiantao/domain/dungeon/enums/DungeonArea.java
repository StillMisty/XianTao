package top.stillmisty.xiantao.domain.dungeon.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DungeonArea {
  OUTER("OUTER", "外围"),
  INNER("INNER", "内围"),
  CORE("CORE", "核心");

  @EnumValue private final String code;
  private final String name;

  DungeonArea(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static DungeonArea fromCode(String code) {
    for (DungeonArea area : values()) {
      if (area.code.equals(code)) return area;
    }
    throw new IllegalArgumentException("Unknown DungeonArea code: " + code);
  }
}
