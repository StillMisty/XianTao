package top.stillmisty.xiantao.domain.team.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum InvitationStatus {
  PENDING("PENDING", "待处理"),
  ACCEPTED("ACCEPTED", "已接受"),
  REJECTED("REJECTED", "已拒绝"),
  EXPIRED("EXPIRED", "已过期");

  @EnumValue private final String code;
  private final String name;

  InvitationStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static InvitationStatus fromCode(String code) {
    for (InvitationStatus status : values()) {
      if (status.code.equals(code)) return status;
    }
    throw new IllegalArgumentException("Unknown InvitationStatus code: " + code);
  }
}
