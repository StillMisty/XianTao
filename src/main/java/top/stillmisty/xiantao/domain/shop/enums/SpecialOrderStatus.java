package top.stillmisty.xiantao.domain.shop.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SpecialOrderStatus {
  PENDING("PENDING", "等待调货"),
  READY("READY", "已到货"),
  COLLECTED("COLLECTED", "已取货"),
  CANCELLED("CANCELLED", "已取消");

  @EnumValue private final String code;

  private final String name;

  SpecialOrderStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static SpecialOrderStatus fromCode(String code) {
    for (SpecialOrderStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown SpecialOrderStatus code: " + code);
  }
}
