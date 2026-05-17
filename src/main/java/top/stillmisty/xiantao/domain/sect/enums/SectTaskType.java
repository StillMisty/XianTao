package top.stillmisty.xiantao.domain.sect.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 宗门任务类型枚举 */
@Getter
public enum SectTaskType {
  HUNT("HUNT", "狩猎"),
  DONATE("DONATE", "捐献");

  @EnumValue private final String code;
  private final String name;

  SectTaskType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static SectTaskType fromCode(String code) {
    for (SectTaskType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未知的任务类型: " + code);
  }
}
