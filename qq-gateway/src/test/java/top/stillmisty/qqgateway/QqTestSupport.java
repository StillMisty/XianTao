package top.stillmisty.qqgateway;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** 测试公共构造。 */
final class QqTestSupport {

  private QqTestSupport() {}

  static QqGatewayConfig config(TestHttpServer server) {
    return QqGatewayConfig.of("app-1", "secret-1")
        .withAppBaseUri(server.baseUri())
        .withApiBaseUri(server.baseUri());
  }

  static QqIncomingMessage groupMessage() {
    return new QqIncomingMessage(
        "EVENT-1", "MSG-1", QqScene.GROUP_AT, "MEMBER-1", "GROUP-1", "状态", "小明", Instant.EPOCH);
  }

  /** 使用当前时间的群 @ 事件（Webhook 新鲜度校验通过）。 */
  static String groupAtEventJson() {
    return groupAtEventJson(Instant.now());
  }

  /** 使用指定时间戳的群 @ 事件（构造过期事件用于防重放测试）。 */
  static String groupAtEventJson(Instant timestamp) {
    return """
        {"op":0,"s":2,"t":"GROUP_AT_MESSAGE_CREATE","id":"EVENT-1","d":{
          "id":"MSG-1","content":"状态","timestamp":"%s",
          "group_openid":"GROUP-1",
          "author":{"id":"AUTHOR-1","member_openid":"MEMBER-1","username":"小明","bot":false}}}
        """
        .formatted(OffsetDateTime.ofInstant(timestamp, ZoneOffset.UTC));
  }

  /** 缺少时间戳的群 @ 事件（新鲜度校验应放行）。 */
  static String groupAtEventJsonWithoutTimestamp() {
    return """
        {"op":0,"s":2,"t":"GROUP_AT_MESSAGE_CREATE","id":"EVENT-NO-TS","d":{
          "id":"MSG-NO-TS","content":"状态","group_openid":"GROUP-1",
          "author":{"id":"AUTHOR-1","member_openid":"MEMBER-1"}}}
        """;
  }
}
