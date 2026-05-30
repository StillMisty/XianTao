package top.stillmisty.xiantao.domain.dungeon.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DungeonArea {
  OUTER("OUTER", "外围", 0),
  INNER("INNER", "内围", 1),
  CORE("CORE", "核心", 2);

  @EnumValue private final String code;
  private final String name;
  private final int rank;

  DungeonArea(String code, String name, int rank) {
    this.code = code;
    this.name = name;
    this.rank = rank;
  }

  public static DungeonArea fromCode(String code) {
    for (DungeonArea area : values()) {
      if (area.code.equals(code)) return area;
    }
    throw new IllegalArgumentException("Unknown DungeonArea code: " + code);
  }
}
