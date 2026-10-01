package top.stillmisty.qqgateway;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * 归一化后的 QQ 消息事件。
 *
 * <p>{@code openId} 与 SimBot 的 {@code getAuthorId()} 取值完全一致：群聊取 {@code author.member_openid}， 单聊取
 * {@code author.user_openid}。切换实现时不得更改，否则现有 {@code user_auth} 绑定关系会失效。
 *
 * @param eventId 平台事件 ID（payload 顶层 {@code id}），用于去重
 * @param messageId 消息 ID（payload {@code d.id}），被动回复的 {@code msg_id}
 * @param scene 消息场景
 * @param openId 用户 openId
 * @param groupOpenId 群 openId，单聊为 null
 * @param content 文本内容
 * @param authorName 发送者昵称，缺失为 null
 * @param timestamp 平台时间戳
 */
public record QqIncomingMessage(
    String eventId,
    String messageId,
    QqScene scene,
    String openId,
    @Nullable String groupOpenId,
    String content,
    @Nullable String authorName,
    Instant timestamp) {

  /** 日志用展示名：昵称（openId 兜底）。 */
  public String displayName() {
    return authorName == null || authorName.isBlank() ? openId : authorName;
  }
}
