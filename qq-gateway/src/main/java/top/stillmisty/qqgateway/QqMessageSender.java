package top.stillmisty.qqgateway;

import org.jspecify.annotations.Nullable;

/**
 * 消息发送接口。
 *
 * <p>被动回复依赖事件携带的 {@code msg_id}，同一消息最多 5 次、5 分钟内有效； 序号（{@code msg_seq}）由实现自动分配，相同 {@code msg_id +
 * msg_seq} 重复发送会被平台拒绝。
 *
 * <p>可选的 {@link QqKeyboard} 用于在消息底部挂载按钮；当平台因兼容性原因拒绝携带按钮的请求时， 实现会自动降级为不带按钮重发，保证消息可达。
 */
public interface QqMessageSender {

  /**
   * 被动回复一条 Markdown 消息（{@code msg_type = 2}）。
   *
   * @param incoming 触发回复的原始消息
   * @param markdown Markdown 内容
   * @throws QqApiException 平台返回错误（含 msg_id 过期、频率限制等）
   */
  default void replyMarkdown(QqIncomingMessage incoming, String markdown) {
    replyMarkdown(incoming, markdown, null);
  }

  /**
   * 被动回复一条带按钮的 Markdown 消息。
   *
   * @param incoming 触发回复的原始消息
   * @param markdown Markdown 内容
   * @param keyboard 按钮键盘，null 表示不带按钮
   * @throws QqApiException 平台返回错误
   */
  void replyMarkdown(QqIncomingMessage incoming, String markdown, @Nullable QqKeyboard keyboard);

  /**
   * 向群聊主动发送 Markdown 消息（{@code msg_type = 2}），受平台主动消息配额约束。
   *
   * @param groupOpenId 群 openId
   * @param markdown Markdown 内容
   * @throws QqApiException 平台返回错误
   */
  default void sendGroupMarkdown(String groupOpenId, String markdown) {
    sendGroupMarkdown(groupOpenId, markdown, null);
  }

  /**
   * 向群聊主动发送带按钮的 Markdown 消息。
   *
   * @param groupOpenId 群 openId
   * @param markdown Markdown 内容
   * @param keyboard 按钮键盘，null 表示不带按钮
   * @throws QqApiException 平台返回错误
   */
  void sendGroupMarkdown(String groupOpenId, String markdown, @Nullable QqKeyboard keyboard);

  /**
   * 向单聊主动发送 Markdown 消息（{@code msg_type = 2}），受平台主动消息配额约束。
   *
   * @param openId 用户 openId
   * @param markdown Markdown 内容
   * @throws QqApiException 平台返回错误
   */
  default void sendC2CMarkdown(String openId, String markdown) {
    sendC2CMarkdown(openId, markdown, null);
  }

  /**
   * 向单聊主动发送带按钮的 Markdown 消息。
   *
   * @param openId 用户 openId
   * @param markdown Markdown 内容
   * @param keyboard 按钮键盘，null 表示不带按钮
   * @throws QqApiException 平台返回错误
   */
  void sendC2CMarkdown(String openId, String markdown, @Nullable QqKeyboard keyboard);
}
