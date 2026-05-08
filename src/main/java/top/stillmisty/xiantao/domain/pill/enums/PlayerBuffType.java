package top.stillmisty.xiantao.domain.pill.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 玩家丹药Buff类型枚举 */
@Getter
public enum PlayerBuffType {
  ATTACK("attack", "攻击"),
  DEFENSE("defense", "防御"),
  SPEED("speed", "速度"),
  BREAKTHROUGH("breakthrough", "突破成功率");

  @EnumValue private final String code;

  private final String displayName;

  PlayerBuffType(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public static PlayerBuffType fromCode(String code) {
    for (PlayerBuffType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown PlayerBuffType code: " + code);
  }
}
