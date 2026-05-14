package top.stillmisty.xiantao.domain.dungeon.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DungeonStatus {
  ACTIVE("ACTIVE", "进行中"),
  COMPLETED("COMPLETED", "已通关"),
  FAILED("FAILED", "战败"),
  ABANDONED("ABANDONED", "已放弃");

  @EnumValue private final String code;
  private final String name;

  DungeonStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static DungeonStatus fromCode(String code) {
    for (DungeonStatus status : values()) {
      if (status.code.equals(code)) return status;
    }
    throw new IllegalArgumentException("Unknown DungeonStatus code: " + code);
  }
}
