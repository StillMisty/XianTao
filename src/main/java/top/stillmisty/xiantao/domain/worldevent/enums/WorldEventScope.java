package top.stillmisty.xiantao.domain.worldevent.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum WorldEventScope {
  GLOBAL("GLOBAL", "全局"),
  REGIONAL("REGIONAL", "区域");

  @EnumValue private final String code;
  private final String name;

  WorldEventScope(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static WorldEventScope fromCode(String code) {
    for (WorldEventScope scope : values()) {
      if (scope.code.equals(code)) {
        return scope;
      }
    }
    throw new IllegalArgumentException("Unknown WorldEventScope code: " + code);
  }
}
