package top.stillmisty.qqgateway;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * QQ OpenAPI HTTP 客户端。
 *
 * <p>只做「发请求 → 得到状态码与响应体」，错误语义由调用方决定；网络异常统一转成 {@link QqApiException}（httpStatus = 0）。
 */
final class QqHttpApi implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(QqHttpApi.class);
  private static final ObjectMapper ERROR_MAPPER = new ObjectMapper();

  private final QqGatewayConfig config;
  private final ObjectMapper mapper;
  private final HttpClient httpClient;
  private final ExecutorService executor;

  QqHttpApi(QqGatewayConfig config, ObjectMapper mapper) {
    this.config = config;
    this.mapper = mapper;
    this.executor = Executors.newVirtualThreadPerTaskExecutor();
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(config.connectTimeout())
            .executor(executor)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
  }

  /** 获取通用 WSS 接入点。 */
  URI fetchGatewayUri(String token) {
    HttpResult result =
        execute(
            HttpRequest.newBuilder(config.apiBaseUri().resolve("/gateway"))
                .timeout(config.requestTimeout())
                .header("Authorization", "QQBot " + token)
                .GET()
                .build());
    if (!result.isSuccess()) {
      throw result.toException("获取 WebSocket 网关地址失败");
    }
    JsonNode node = parseJson(result.body(), "解析网关地址响应失败");
    String url = node.path("url").asText("");
    if (url.isBlank()) {
      throw new QqApiException(result.status(), node.path("code").asText(""), "网关地址响应缺少 url 字段");
    }
    return URI.create(url);
  }

  /** 鉴权用 POST（无 Authorization 头），用于获取 App Access Token。 */
  HttpResult postForm(URI uri, ObjectNode body) {
    return execute(
        HttpRequest.newBuilder(uri)
            .timeout(config.requestTimeout())
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
            .build());
  }

  /** 带鉴权的 JSON POST。 */
  void postJson(URI uri, String token, ObjectNode body) {
    HttpResult result =
        execute(
            HttpRequest.newBuilder(uri)
                .timeout(config.requestTimeout())
                .header("Authorization", "QQBot " + token)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build());
    if (!result.isSuccess()) {
      throw result.toException("调用 OpenAPI 失败");
    }
  }

  URI messageUri(QqScene scene, @Nullable String groupOpenId, String openId) {
    if (scene.isGroup()) {
      if (groupOpenId == null || groupOpenId.isBlank()) {
        throw new IllegalArgumentException("群消息缺少 group_openid");
      }
      return config
          .apiBaseUri()
          .resolve("/v2/groups/" + encodePathSegment(groupOpenId) + "/messages");
    }
    return config.apiBaseUri().resolve("/v2/users/" + encodePathSegment(openId) + "/messages");
  }

  JsonNode parseJson(byte[] body, String errorPrefix) {
    try {
      return mapper.readTree(body);
    } catch (RuntimeException e) {
      throw new QqApiException(
          0, null, errorPrefix + ": " + truncate(new String(body, StandardCharsets.UTF_8)), e);
    }
  }

  private HttpResult execute(HttpRequest request) {
    try {
      HttpResponse<byte[]> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
      return new HttpResult(
          response.statusCode(), response.body(), response.headers().firstValue("Retry-After"));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new QqApiException(0, null, "请求被中断", e);
    } catch (IOException e) {
      throw new QqApiException(0, null, "网络请求失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }

  /** 最小可用保真的 path 段编码：只保留 unreserved 字符。 */
  static String encodePathSegment(String segment) {
    StringBuilder sb = new StringBuilder(segment.length());
    for (byte b : segment.getBytes(StandardCharsets.UTF_8)) {
      char c = (char) (b & 0xFF);
      if ((c >= 'a' && c <= 'z')
          || (c >= 'A' && c <= 'Z')
          || (c >= '0' && c <= '9')
          || c == '-'
          || c == '.'
          || c == '_'
          || c == '~') {
        sb.append(c);
      } else {
        sb.append('%')
            .append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16)))
            .append(Character.toUpperCase(Character.forDigit(b & 0xF, 16)));
      }
    }
    return sb.toString();
  }

  private static String truncate(String text) {
    return text.length() <= 300 ? text : text.substring(0, 300) + "...";
  }

  /** HTTP 调用结果。 */
  static final class HttpResult {

    private final int status;
    private final byte[] body;
    private final Optional<String> retryAfterHeader;

    HttpResult(int status, byte[] body, Optional<String> retryAfterHeader) {
      this.status = status;
      this.body = body;
      this.retryAfterHeader = retryAfterHeader;
    }

    int status() {
      return status;
    }

    byte[] body() {
      return body;
    }

    boolean isSuccess() {
      return status >= 200 && status < 300;
    }

    String bodyText() {
      return new String(body, StandardCharsets.UTF_8);
    }

    long retryAfterMillis(long fallbackMillis) {
      return retryAfterHeader
          .map(
              value -> {
                try {
                  return Duration.ofSeconds(Long.parseLong(value.trim())).toMillis();
                } catch (NumberFormatException ignored) {
                  return fallbackMillis;
                }
              })
          .orElse(fallbackMillis);
    }

    QqApiException toException(String prefix) {
      @Nullable String code = null;
      @Nullable String message = null;
      try {
        JsonNode node = ERROR_MAPPER.readTree(body);
        code = blankToNull(node.path("code").asText(""));
        message =
            blankToNull(
                firstNonBlank(node.path("message").asText(""), node.path("msg").asText("")));
      } catch (RuntimeException ignored) {
        // 响应体不是 JSON，直接用原文
      }
      String detail = message != null ? message : bodyText();
      String codePart = code != null ? ", code " + code : "";
      log.debug("QQ API 调用失败: HTTP {}{}, body={}", status, codePart, truncate(bodyText()));
      return new QqApiException(
          status,
          code,
          prefix + " (HTTP " + status + codePart + "): " + detail,
          null,
          retryAfterMillis(0));
    }

    private static @Nullable String blankToNull(String value) {
      return value.isBlank() ? null : value;
    }

    private static String firstNonBlank(String first, String second) {
      return first.isBlank() ? second : first;
    }
  }
}
