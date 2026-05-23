package top.stillmisty.xiantao.domain.worldevent.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum WorldEventStatus {
  UPCOMING("UPCOMING", "预告"),
  ACTIVE("ACTIVE", "进行中"),
  ENDING("ENDING", "收尾"),
  EXPIRED("EXPIRED", "已过期");

  @EnumValue private final String code;
  private final String name;

  WorldEventStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static WorldEventStatus fromCode(String code) {
    for (WorldEventStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown WorldEventStatus code: " + code);
  }
}
