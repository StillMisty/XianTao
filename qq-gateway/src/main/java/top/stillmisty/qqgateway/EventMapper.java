package top.stillmisty.qqgateway;

import java.time.Instant;
import java.time.OffsetDateTime;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * QQ 事件 payload → {@link QqIncomingMessage} 映射。
 *
 * <p>openId 取值遵循 SimBot 的既有语义（群聊 {@code author.member_openid}、单聊 {@code author.user_openid}）， 保证
 * {@code user_auth} 绑定关系不变。
 */
final class EventMapper {

  private static final Logger log = LoggerFactory.getLogger(EventMapper.class);

  private EventMapper() {}

  /**
   * 映射一条消息事件。
   *
   * @param payload WebSocket 或 Webhook 的事件 envelope（含 {@code t} 与 {@code d}）
   * @return 映射结果；非消息事件或字段缺失时返回 null
   */
  static @Nullable QqIncomingMessage map(JsonNode payload) {
    String wireType = payload.path("t").asText("");
    QqScene scene = QqScene.fromWireType(wireType);
    if (scene == null) {
      return null;
    }

    JsonNode data = payload.path("d");
    JsonNode author = data.path("author");

    String messageId = data.path("id").asText("");
    String openId =
        switch (scene) {
          case GROUP_AT, GROUP_MESSAGE ->
              firstNonBlank(author.path("member_openid").asText(""), author.path("id").asText(""));
          case C2C ->
              firstNonBlank(author.path("user_openid").asText(""), author.path("id").asText(""));
        };
    if (messageId.isBlank() || openId.isBlank()) {
      log.warn("QQ 消息事件字段缺失，已忽略: type={}, messageId={}, openId={}", wireType, messageId, openId);
      return null;
    }

    String eventId = firstNonBlank(payload.path("id").asText(""), messageId);
    String groupOpenId = data.path("group_openid").asText("");
    String authorName =
        firstNonBlank(author.path("username").asText(""), author.path("nickname").asText(""));
    Instant timestamp = timestampOf(payload);

    return new QqIncomingMessage(
        eventId,
        messageId,
        scene,
        openId,
        groupOpenId.isBlank() ? null : groupOpenId,
        data.path("content").asText(""),
        authorName.isBlank() ? null : authorName,
        timestamp == null ? Instant.EPOCH : timestamp);
  }

  /**
   * 提取事件时间戳：优先 {@code d.timestamp}（ISO-8601），其次 {@code d.event_ts}（Unix 秒）。
   *
   * @return 时间戳；缺失或无法解析时返回 null
   */
  static @Nullable Instant timestampOf(JsonNode payload) {
    JsonNode data = payload.path("d");
    String text = data.path("timestamp").asText("");
    if (!text.isBlank()) {
      try {
        return OffsetDateTime.parse(text).toInstant();
      } catch (RuntimeException e) {
        log.debug("无法解析事件时间戳: {}", text);
      }
    }
    JsonNode eventTs = data.path("event_ts");
    if (eventTs.isNumber()) {
      return Instant.ofEpochSecond(eventTs.asLong());
    }
    if (eventTs.isTextual() && !eventTs.asText().isBlank()) {
      try {
        return Instant.ofEpochSecond(Long.parseLong(eventTs.asText().trim()));
      } catch (NumberFormatException e) {
        log.debug("无法解析 event_ts: {}", eventTs.asText());
      }
    }
    return null;
  }

  private static String firstNonBlank(String first, String second) {
    return first.isBlank() ? second : first;
  }
}
