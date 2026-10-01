package top.stillmisty.qqgateway;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Webhook 回调处理器：签名校验 → 新鲜度校验 → op 13 地址验证 → 事件映射与去重 → 异步交付。
 *
 * <p>与具体 Web 框架无关：调用方把原始请求体与 {@code X-Signature-Ed25519} 头传入，按返回的状态码与响应体写回即可。
 * 事件在虚拟线程上异步处理，保证回调在平台超时前得到 ACK。
 *
 * <p>防重放：除签名校验与事件去重外，还会拒绝时间戳超出 {@code maxEventAge} 的已签名事件（默认 5 分钟，可用 {@link Duration#ZERO}
 * 关闭）。事件缺少时间戳时跳过该项校验（放行并记录 debug 日志），避免误伤平台新增事件类型。
 */
public final class QqWebhookHandler implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(QqWebhookHandler.class);

  /** 默认事件新鲜度窗口。 */
  public static final Duration DEFAULT_MAX_EVENT_AGE = Duration.ofMinutes(5);

  private static final int OP_DISPATCH = 0;
  private static final int OP_HTTP_CALLBACK_ACK = 12;
  private static final int OP_VALIDATION = 13;

  private final QqWebhookVerifier verifier;
  private final ObjectMapper mapper;
  private final QqEventListener listener;
  private final Duration maxEventAge;
  private final EventDeduplicator deduplicator = new EventDeduplicator();
  private final ExecutorService eventExecutor = Executors.newVirtualThreadPerTaskExecutor();

  public QqWebhookHandler(QqBotClient client, QqEventListener listener) {
    this(client, listener, DEFAULT_MAX_EVENT_AGE);
  }

  public QqWebhookHandler(QqBotClient client, QqEventListener listener, Duration maxEventAge) {
    this(
        new QqWebhookVerifier(client.config().clientSecret()),
        client.mapper(),
        listener,
        maxEventAge);
  }

  QqWebhookHandler(
      QqWebhookVerifier verifier,
      ObjectMapper mapper,
      QqEventListener listener,
      Duration maxEventAge) {
    this.verifier = verifier;
    this.mapper = mapper;
    this.listener = listener;
    this.maxEventAge = maxEventAge;
  }

  /**
   * 处理一次回调请求。
   *
   * @param rawBody 原始请求体（验签必须使用未改动的字节）
   * @param signatureHex 请求头 {@code X-Signature-Ed25519}，缺失为 null
   * @return 写回给平台的 HTTP 状态码与响应体
   */
  public WebhookResult handle(byte[] rawBody, @Nullable String signatureHex) {
    JsonNode payload;
    try {
      payload = mapper.readTree(rawBody);
    } catch (RuntimeException e) {
      log.warn("Webhook 请求体不是合法 JSON: {}", e.toString());
      return new WebhookResult(400, errorBody("bad request"));
    }

    int op = payload.path("op").asInt(-1);
    if (op == OP_VALIDATION) {
      return handleValidation(payload);
    }

    if (!verifier.verify(signatureHex == null ? "" : signatureHex, rawBody)) {
      log.warn("Webhook 签名校验失败，已拒绝");
      return new WebhookResult(401, errorBody("invalid signature"));
    }

    if (op == OP_DISPATCH) {
      if (!isWithinFreshnessWindow(payload)) {
        return new WebhookResult(401, errorBody("stale event"));
      }
      QqIncomingMessage message = EventMapper.map(payload);
      if (message != null) {
        if (deduplicator.firstSeen(message.eventId())) {
          eventExecutor.execute(() -> deliver(message));
        } else {
          log.debug("忽略重复事件: {}", message.eventId());
        }
      }
    }

    ObjectNode ack = mapper.createObjectNode();
    ack.put("op", OP_HTTP_CALLBACK_ACK);
    return new WebhookResult(200, ack.toString());
  }

  /** 事件新鲜度校验：拒绝时间戳超出窗口的已签名事件，防重放。 */
  private boolean isWithinFreshnessWindow(JsonNode payload) {
    if (maxEventAge.isZero() || maxEventAge.isNegative()) {
      return true;
    }
    Instant timestamp = EventMapper.timestampOf(payload);
    if (timestamp == null) {
      log.debug("Webhook 事件缺少时间戳，跳过新鲜度校验: eventId={}", payload.path("id").asText(""));
      return true;
    }
    Duration age = Duration.between(timestamp, Instant.now()).abs();
    if (age.compareTo(maxEventAge) > 0) {
      log.warn(
          "Webhook 事件超出新鲜度窗口，已拒绝: eventId={}, age={}s, window={}s",
          payload.path("id").asText(""),
          age.toSeconds(),
          maxEventAge.toSeconds());
      return false;
    }
    return true;
  }

  private WebhookResult handleValidation(JsonNode payload) {
    JsonNode data = payload.path("d");
    String plainToken = data.path("plain_token").asText("");
    String eventTs = data.path("event_ts").asText("");
    if (plainToken.isBlank() || eventTs.isBlank()) {
      log.warn("回调地址验证请求缺少 plain_token/event_ts");
      return new WebhookResult(400, errorBody("invalid challenge"));
    }
    ObjectNode response = mapper.createObjectNode();
    response.put("plain_token", plainToken);
    response.put("signature", verifier.signChallenge(plainToken, eventTs));
    return new WebhookResult(200, response.toString());
  }

  private void deliver(QqIncomingMessage message) {
    try {
      listener.onMessage(message);
    } catch (Exception e) {
      log.error("处理 QQ 消息事件失败: eventId={}, scene={}", message.eventId(), message.scene(), e);
    }
  }

  @Override
  public void close() {
    eventExecutor.shutdown();
    try {
      eventExecutor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static String errorBody(String message) {
    return "{\"error\":\"" + message + "\"}";
  }

  /** 回调处理结果。 */
  public record WebhookResult(int status, String body) {}
}
