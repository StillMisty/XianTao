package top.stillmisty.xiantao.domain.bounty.enums;

import com.mybatisflex.annotation.EnumValue;
import java.util.Arrays;
import lombok.Getter;

/** 悬赏状态 */
@Getter
public enum BountyStatus {
  ACTIVE("ACTIVE", "进行中"),
  COMPLETED("COMPLETED", "已完成"),
  ABANDONED("ABANDONED", "已放弃");

  @EnumValue private final String code;
  private final String name;

  BountyStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static BountyStatus fromCode(String code) {
    return Arrays.stream(values())
        .filter(s -> s.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown BountyStatus code: " + code));
  }
}
