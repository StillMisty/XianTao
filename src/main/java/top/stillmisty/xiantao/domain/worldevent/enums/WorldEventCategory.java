package top.stillmisty.xiantao.domain.worldevent.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum WorldEventCategory {
  ECONOMIC("ECONOMIC", "经济"),
  ENVIRONMENTAL("ENVIRONMENTAL", "环境"),
  NARRATIVE("NARRATIVE", "叙事"),
  PARTICIPATORY("PARTICIPATORY", "参与");

  @EnumValue private final String code;
  private final String name;

  WorldEventCategory(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static WorldEventCategory fromCode(String code) {
    for (WorldEventCategory category : values()) {
      if (category.code.equals(code)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown WorldEventCategory code: " + code);
  }
}
