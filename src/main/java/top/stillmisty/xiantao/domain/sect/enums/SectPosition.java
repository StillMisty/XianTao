package top.stillmisty.xiantao.domain.sect.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 宗门职位枚举 */
@Getter
public enum SectPosition {
  LEADER("LEADER", "宗主"),
  VICE_LEADER("VICE_LEADER", "副宗主"),
  ELDER("ELDER", "长老"),
  ELITE("ELITE", "精英"),
  MEMBER("MEMBER", "普通成员");

  @EnumValue private final String code;
  private final String name;

  SectPosition(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static SectPosition fromCode(String code) {
    for (SectPosition pos : values()) {
      if (pos.code.equals(code)) {
        return pos;
      }
    }
    throw new IllegalArgumentException("未知的宗门职位: " + code);
  }
}
