package top.stillmisty.xiantao.domain.event.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 活动类型枚举 — 用户当前在做什么 */
@Getter
public enum ActivityType {
  TRAVEL("TRAVEL", "旅行"),
  TRAINING("TRAINING", "历练"),
  BOUNTY("BOUNTY", "悬赏");

  @EnumValue private final String code;
  private final String name;

  ActivityType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static ActivityType fromCode(String code) {
    for (ActivityType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ActivityType code: " + code);
  }
}
