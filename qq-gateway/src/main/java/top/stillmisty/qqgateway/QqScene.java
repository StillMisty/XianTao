package top.stillmisty.qqgateway;

import org.jspecify.annotations.Nullable;

/** QQ 消息事件场景。 */
public enum QqScene {
  /** 群内 @ 机器人消息（{@code GROUP_AT_MESSAGE_CREATE}）。 */
  GROUP_AT("GROUP_AT_MESSAGE_CREATE"),
  /** 群内全量消息（{@code GROUP_MESSAGE_CREATE}），需群主开启"接收所有消息"。 */
  GROUP_MESSAGE("GROUP_MESSAGE_CREATE"),
  /** 单聊消息（{@code C2C_MESSAGE_CREATE}）。 */
  C2C("C2C_MESSAGE_CREATE");

  private final String wireType;

  QqScene(String wireType) {
    this.wireType = wireType;
  }

  /** 平台事件类型名（payload 中的 {@code t} 字段）。 */
  public String wireType() {
    return wireType;
  }

  /** 按平台事件类型名解析场景，未知类型返回 null。 */
  public static @Nullable QqScene fromWireType(String type) {
    for (QqScene scene : values()) {
      if (scene.wireType.equals(type)) {
        return scene;
      }
    }
    return null;
  }

  /** 是否群聊场景。 */
  public boolean isGroup() {
    return this != C2C;
  }
}
