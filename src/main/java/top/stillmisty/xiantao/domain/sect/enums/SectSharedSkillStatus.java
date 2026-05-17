package top.stillmisty.xiantao.domain.sect.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 宗门共享功法状态枚举 */
@Getter
public enum SectSharedSkillStatus {
  PENDING("PENDING", "待上架"),
  LISTED("LISTED", "已上架");

  @EnumValue private final String code;
  private final String name;

  SectSharedSkillStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static SectSharedSkillStatus fromCode(String code) {
    for (SectSharedSkillStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("未知的功法状态: " + code);
  }
}
