package top.stillmisty.xiantao.domain.sect.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 对话角色枚举 */
@Getter
public enum ChatRole {
  USER("user", "用户"),
  ASSISTANT("assistant", "助手"),
  SYSTEM("system", "系统"),
  TOOL("tool", "工具"),
  /** ChatHistory.extraData 中用于存储 DeepSeek reasoning_content */
  REASONING_CONTENT("reasoning_content", "推理内容");

  @EnumValue private final String code;
  private final String name;

  ChatRole(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static ChatRole fromCode(String code) {
    for (ChatRole role : values()) {
      if (role.code.equals(code)) {
        return role;
      }
    }
    throw new IllegalArgumentException("未知的对话角色: " + code);
  }
}
