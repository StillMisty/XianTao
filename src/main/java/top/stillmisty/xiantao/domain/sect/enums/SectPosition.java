package top.stillmisty.xiantao.domain.sect.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 宗门职位枚举 */
@Getter
public enum SectPosition {
  LEADER("LEADER", "宗主", 0),
  ELDER("ELDER", "长老", 1),
  MEMBER("MEMBER", "弟子", 2);

  @EnumValue private final String code;
  private final String name;
  private final int rank;

  SectPosition(String code, String name, int rank) {
    this.code = code;
    this.name = name;
    this.rank = rank;
  }

  public static SectPosition fromCode(String code) {
    for (SectPosition pos : values()) {
      if (pos.code.equals(code)) {
        return pos;
      }
    }
    throw new IllegalArgumentException("未知的宗门职位: " + code);
  }

  public boolean isHigherThan(SectPosition other) {
    return this.rank < other.rank;
  }

  public boolean canManage() {
    return this == LEADER || this == ELDER;
  }

  public boolean canInvite() {
    return this == LEADER || this == ELDER;
  }

  public boolean canKick() {
    return this == LEADER || this == ELDER;
  }

  public boolean canPostNotice() {
    return this == LEADER || this == ELDER;
  }

  public boolean canManageSkills() {
    return this == LEADER || this == ELDER;
  }
}
