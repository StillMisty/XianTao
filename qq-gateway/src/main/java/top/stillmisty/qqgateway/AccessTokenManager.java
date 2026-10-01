package top.stillmisty.qqgateway;

import java.time.Duration;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * App Access Token 管理器。
 *
 * <p>Token 有效期约 2 小时，这里在剩余时间低于 {@code tokenRefreshAhead} 时刷新； 刷新过程单飞行（single-flight），并发调用只触发一次网络请求。
 */
final class AccessTokenManager {

  private static final Logger log = LoggerFactory.getLogger(AccessTokenManager.class);
  private static final Duration FALLBACK_TTL = Duration.ofMinutes(30);

  private final QqGatewayConfig config;
  private final QqHttpApi api;
  private final ObjectMapper mapper;

  private final Object lock = new Object();
  private volatile @Nullable CachedToken cached;

  AccessTokenManager(QqGatewayConfig config, QqHttpApi api, ObjectMapper mapper) {
    this.config = config;
    this.api = api;
    this.mapper = mapper;
  }

  /** 返回有效 token；必要时刷新。 */
  String token() {
    CachedToken current = cached;
    if (current != null && !current.needsRefresh(config.tokenRefreshAhead())) {
      return current.value;
    }
    synchronized (lock) {
      CachedToken again = cached;
      if (again != null && !again.needsRefresh(config.tokenRefreshAhead())) {
        return again.value;
      }
      try {
        CachedToken refreshed = fetch();
        cached = refreshed;
        return refreshed.value;
      } catch (QqApiException e) {
        // 刷新失败但旧 token 尚未真正过期：继续使用，避免网络抖动直接打断请求
        if (again != null && Instant.now().isBefore(again.expiresAt)) {
          log.warn("刷新 Access Token 失败，继续使用未过期的旧 token: {}", e.getMessage());
          return again.value;
        }
        throw e;
      }
    }
  }

  /** 标记当前 token 失效，下次调用强制刷新。 */
  void invalidate() {
    cached = null;
  }

  private CachedToken fetch() {
    ObjectNode body = mapper.createObjectNode();
    body.put("appId", config.appId());
    body.put("clientSecret", config.clientSecret());

    QqHttpApi.HttpResult result =
        api.postForm(config.appBaseUri().resolve("/app/getAppAccessToken"), body);
    if (!result.isSuccess()) {
      throw result.toException("获取 App Access Token 失败");
    }
    JsonNode node = readTree(result.body());
    String token = node.path("access_token").asText("");
    if (token.isBlank()) {
      throw new QqApiException(
          result.status(),
          node.path("code").asText(""),
          "获取 App Access Token 失败: " + truncate(result.bodyText()));
    }
    Duration ttl = parseSeconds(node.path("expires_in"));
    log.info("QQ Access Token 已刷新，有效期 {} 秒", ttl.toSeconds());
    return new CachedToken(token, Instant.now().plus(ttl));
  }

  private JsonNode readTree(byte[] body) {
    try {
      return mapper.readTree(body);
    } catch (RuntimeException e) {
      throw new QqApiException(
          0,
          null,
          "解析 Token 响应失败: " + truncate(new String(body, java.nio.charset.StandardCharsets.UTF_8)),
          e);
    }
  }

  private static Duration parseSeconds(JsonNode node) {
    if (node.isNumber()) {
      return Duration.ofSeconds(Math.max(1, node.asLong()));
    }
    if (node.isTextual()) {
      try {
        return Duration.ofSeconds(Math.max(1, Long.parseLong(node.asText().trim())));
      } catch (NumberFormatException ignored) {
        // 落到兜底值
      }
    }
    return FALLBACK_TTL;
  }

  private static String truncate(String text) {
    return text.length() <= 200 ? text : text.substring(0, 200) + "...";
  }

  private record CachedToken(String value, Instant expiresAt) {

    boolean needsRefresh(Duration ahead) {
      return Instant.now().isAfter(expiresAt.minus(ahead));
    }
  }
}
