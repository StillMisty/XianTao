package top.stillmisty.xiantao.domain.team.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum TeamStatus {
  ACTIVE("ACTIVE", "活跃"),
  DISBANDED("DISBANDED", "已解散");

  @EnumValue private final String code;
  private final String name;

  TeamStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static TeamStatus fromCode(String code) {
    for (TeamStatus status : values()) {
      if (status.code.equals(code)) return status;
    }
    throw new IllegalArgumentException("Unknown TeamStatus code: " + code);
  }
}
