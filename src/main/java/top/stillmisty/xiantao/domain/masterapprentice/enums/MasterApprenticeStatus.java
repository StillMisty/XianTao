package top.stillmisty.xiantao.domain.masterapprentice.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 师徒关系状态枚举 */
@Getter
public enum MasterApprenticeStatus {
  ACTIVE("ACTIVE", "在师"),
  GRADUATED("GRADUATED", "已出师"),
  DISMISSED("DISMISSED", "已逐出"),
  RENEGED("RENEGED", "已叛师");

  @EnumValue private final String code;
  private final String name;

  MasterApprenticeStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static MasterApprenticeStatus fromCode(String code) {
    for (MasterApprenticeStatus s : values()) {
      if (s.code.equals(code)) {
        return s;
      }
    }
    throw new IllegalArgumentException("未知的师徒状态: " + code);
  }
}
