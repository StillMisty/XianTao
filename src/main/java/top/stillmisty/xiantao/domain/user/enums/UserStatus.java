package top.stillmisty.xiantao.domain.user.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 用户状态枚举 */
@Getter
public enum UserStatus {

  /** 空闲 */
  IDLE("IDLE", "空闲"),

  /** 历练 */
  TRAINING("TRAINING", "历练"),

  /** 赶路 */
  TRAVELING("TRAVELING", "赶路"),

  /** 悬赏 */
  BOUNTY("BOUNTY", "悬赏"),

  /** 濒死 */
  DYING("DYING", "濒死");

  @EnumValue private final String code;
  private final String name;

  UserStatus(String code, String name) {
    this.code = code;
    this.name = name;
  }
}
