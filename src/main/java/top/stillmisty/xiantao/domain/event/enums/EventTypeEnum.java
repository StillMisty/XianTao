package top.stillmisty.xiantao.domain.event.enums;

import com.mybatisflex.annotation.EnumValue;
import java.util.Arrays;

public enum EventTypeEnum {
  NUMERIC("NUMERIC"),
  COMBAT("COMBAT"),
  CHOICE("CHOICE");

  @EnumValue private final String code;

  EventTypeEnum(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static EventTypeEnum fromCode(String code) {
    return Arrays.stream(values())
        .filter(t -> t.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown event type: " + code));
  }
}
