package top.stillmisty.qqgateway;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * QQ 消息按钮（自定义按钮 keyboard 中的单个 button）。
 *
 * <p>字段语义见官方文档「消息按钮」：
 *
 * <ul>
 *   <li>{@code actionType}：0 链接、1 回调（触发 INTERACTION_CREATE）、2 指令（点击后发送 {@code data}）
 *   <li>{@code style}：0 灰色线框、1 蓝色线框
 *   <li>{@code specifyUserIds}：为空表示所有人可点击；非空时仅这些用户可点击
 *   <li>{@code enter}：指令按钮专用，true 表示点击后直接发送，false 表示填入输入框
 *   <li>{@code unsupportTips}：客户端不支持该按钮时弹出的提示文案（文档标记为必填）
 * </ul>
 */
public record QqButton(
    String id,
    String label,
    String visitedLabel,
    int style,
    int actionType,
    String data,
    @Nullable List<String> specifyUserIds,
    boolean enter,
    String unsupportTips) {

  public static final int ACTION_LINK = 0;
  public static final int ACTION_CALLBACK = 1;
  public static final int ACTION_COMMAND = 2;

  public static final int STYLE_GREY = 0;
  public static final int STYLE_BLUE = 1;

  public QqButton {
    requireText(id, "id");
    requireText(label, "label");
    requireText(visitedLabel, "visitedLabel");
    requireText(data, "data");
    requireText(unsupportTips, "unsupportTips");
    if (style != STYLE_GREY && style != STYLE_BLUE) {
      throw new IllegalArgumentException("不支持的按钮样式: " + style);
    }
    if (actionType != ACTION_LINK
        && actionType != ACTION_CALLBACK
        && actionType != ACTION_COMMAND) {
      throw new IllegalArgumentException("不支持的按钮动作类型: " + actionType);
    }
    if (specifyUserIds != null
        && (specifyUserIds.isEmpty() || specifyUserIds.stream().anyMatch(String::isBlank))) {
      throw new IllegalArgumentException("specifyUserIds 为空时应传 null");
    }
  }

  /**
   * 指令按钮：点击后发送 {@code data}（群聊自动 @机器人），所有人可点击。
   *
   * @param id 按钮 ID（同一 keyboard 内唯一）
   * @param label 按钮文字
   * @param data 点击后发送的文本
   */
  public static QqButton command(String id, String label, String data) {
    return new QqButton(
        id,
        label,
        label,
        STYLE_BLUE,
        ACTION_COMMAND,
        data,
        null,
        true,
        "当前客户端暂不支持按钮，请手动发送：" + data);
  }

  /** 限制为指定用户可点击（传 null/空表示所有人）。 */
  public QqButton forUsers(@Nullable List<String> userIds) {
    return new QqButton(
        id, label, visitedLabel, style, actionType, data, userIds, enter, unsupportTips);
  }

  private static void requireText(String value, String field) {
    Objects.requireNonNull(value, field);
    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " 不能为空");
    }
  }
}
