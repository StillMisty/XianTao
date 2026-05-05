package top.stillmisty.xiantao.domain.user.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PlatformType {
  ONE_BOT_V11("OneBotV11", "OneBotV11"),
  QQ("QQ", "QQ"),
  WEB("Web", "Web");

  @EnumValue private final String code;
  private final String name;

  PlatformType(String code, String name) {
    this.code = code;
    this.name = name;
  }
}
