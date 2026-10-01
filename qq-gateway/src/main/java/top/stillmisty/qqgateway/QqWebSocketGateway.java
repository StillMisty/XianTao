package top.stillmisty.qqgateway;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * QQ WebSocket 长连接网关。
 *
 * <p>职责：获取接入点 → Identify/Resume 鉴权 → 心跳与 ACK 检测 → 断线指数退避重连 → 事件去重 → 逐事件交付给监听器。
 *
 * <p>事件监听器在虚拟线程上调用，一个事件一个线程；{@link #close()} 后停止重连。
 */
public final class QqWebSocketGateway implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(QqWebSocketGateway.class);

  private static final int OP_DISPATCH = 0;
  private static final int OP_HEARTBEAT = 1;
  private static final int OP_IDENTIFY = 2;
  private static final int OP_RESUME = 6;
  private static final int OP_RECONNECT = 7;
  private static final int OP_INVALID_SESSION = 9;
  private static final int OP_HELLO = 10;
  private static final int OP_HEARTBEAT_ACK = 11;

  private static final long DEFAULT_HEARTBEAT_INTERVAL_MILLIS = 45_000;
  private static final Duration MIN_BACKOFF = Duration.ofSeconds(1);
  private static final Duration MAX_BACKOFF = Duration.ofSeconds(60);
  private static final Duration DEFAULT_AUTH_TIMEOUT = Duration.ofSeconds(30);

  private final QqBotClient client;
  private final QqEventListener listener;
  private final WsTransportFactory transportFactory;
  private final Duration authTimeout;
  private final ObjectMapper mapper;
  private final EventDeduplicator deduplicator = new EventDeduplicator();

  private final ExecutorService eventExecutor = Executors.newVirtualThreadPerTaskExecutor();
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "qq-gateway-heartbeat");
            thread.setDaemon(true);
            return thread;
          });

  private final AtomicBoolean closed = new AtomicBoolean();
  private final Object connectionLock = new Object();

  private @Nullable WsTransport transport;
  private @Nullable ScheduledFuture<?> heartbeatTask;
  private @Nullable ScheduledFuture<?> authWatchdogTask;
  private @Nullable String sessionId;
  private long lastSeq;
  private volatile boolean heartbeatAckPending;
  private volatile boolean ready;
  private volatile @Nullable Thread supervisor;

  public QqWebSocketGateway(QqBotClient client, QqEventListener listener) {
    this(client, listener, () -> new JdkWsTransport(client.config().connectTimeout()));
  }

  QqWebSocketGateway(
      QqBotClient client, QqEventListener listener, WsTransportFactory transportFactory) {
    this(client, listener, transportFactory, DEFAULT_AUTH_TIMEOUT);
  }

  QqWebSocketGateway(
      QqBotClient client,
      QqEventListener listener,
      WsTransportFactory transportFactory,
      Duration authTimeout) {
    this.client = client;
    this.listener = listener;
    this.transportFactory = transportFactory;
    this.authTimeout = authTimeout;
    this.mapper = client.mapper();
  }

  /** 启动后台连接与重连循环（非阻塞）。重复调用抛异常。 */
  public synchronized void start() {
    if (closed.get()) {
      throw new IllegalStateException("网关已关闭");
    }
    if (supervisor != null) {
      throw new IllegalStateException("网关已启动");
    }
    supervisor = Thread.ofVirtual().name("qq-gateway-supervisor").start(this::supervise);
  }

  /** 是否已鉴权成功（READY/RESUMED）。 */
  public boolean isConnected() {
    return ready && !closed.get();
  }

  @Override
  public void close() {
    if (!closed.compareAndSet(false, true)) {
      return;
    }
    closeTransport();
    stopHeartbeat();
    scheduler.shutdownNow();
    eventExecutor.shutdown();
    Thread thread = supervisor;
    if (thread != null) {
      thread.interrupt();
    }
    try {
      eventExecutor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  // ==================== 连接管理 ====================

  private void supervise() {
    Duration backoff = MIN_BACKOFF;
    while (!closed.get()) {
      try {
        runSession();
        backoff = MIN_BACKOFF;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } catch (Exception e) {
        log.warn("QQ WebSocket 会话中断: {}", e.toString());
      }
      if (closed.get()) {
        return;
      }
      long waitMillis = backoff.toMillis() + ThreadLocalRandom.current().nextLong(0, 500);
      log.info("{} ms 后重连 QQ WebSocket", waitMillis);
      try {
        Thread.sleep(waitMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      backoff = min(backoff.multipliedBy(2));
    }
  }

  private void runSession() throws Exception {
    String accessToken = client.tokens().token();
    URI gatewayUri = client.api().fetchGatewayUri(accessToken);

    CountDownLatch finished = new CountDownLatch(1);
    AtomicReference<@Nullable Throwable> failure = new AtomicReference<>(null);
    WsTransport connection = transportFactory.create();
    synchronized (connectionLock) {
      transport = connection;
    }

    try {
      connection.connect(
          gatewayUri,
          new WsTransport.Listener() {
            @Override
            public void onOpen() {
              log.debug("QQ WebSocket 已建立: {}", gatewayUri);
            }

            @Override
            public void onText(String payload) {
              onPayload(connection, payload);
            }

            @Override
            public void onClosed(int statusCode, String reason) {
              ready = false;
              log.info("QQ WebSocket 已关闭: code={}, reason={}", statusCode, reason);
              finished.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
              ready = false;
              failure.compareAndSet(null, error);
              finished.countDown();
            }
          });
      scheduleAuthWatchdog(connection);
      finished.await();
      Throwable error = failure.get();
      if (error != null) {
        throw new IllegalStateException("WebSocket 连接失败: " + error, error);
      }
    } finally {
      cancelAuthWatchdog();
      stopHeartbeat();
      ready = false;
      connection.close();
      clearTransportIfCurrent(connection);
    }
  }

  private void onPayload(WsTransport connection, String payload) {
    JsonNode node;
    try {
      node = mapper.readTree(payload);
    } catch (RuntimeException e) {
      log.warn("无法解析 QQ 事件 payload，已忽略: {}", abbreviate(payload));
      return;
    }

    int op = node.path("op").asInt(-1);
    switch (op) {
      case OP_HELLO -> {
        long interval =
            node.path("d").path("heartbeat_interval").asLong(DEFAULT_HEARTBEAT_INTERVAL_MILLIS);
        startHeartbeat(connection, interval);
        try {
          sendIdentifyOrResume(connection);
        } catch (RuntimeException e) {
          // 鉴权请求发送失败（如 Token 刷新失败），主动断开触发重连，避免连接空转
          log.warn("发送鉴权请求失败，主动重连: {}", e.toString());
          closeTransportIfCurrent(connection);
        }
      }
      case OP_HEARTBEAT_ACK -> heartbeatAckPending = false;
      case OP_HEARTBEAT -> sendHeartbeat(connection);
      case OP_RECONNECT -> {
        log.info("平台要求重连");
        closeTransport();
      }
      case OP_INVALID_SESSION -> {
        log.warn("平台会话失效，将重新鉴权");
        synchronized (connectionLock) {
          sessionId = null;
          lastSeq = 0;
        }
        closeTransport();
      }
      case OP_DISPATCH -> onDispatch(node);
      default -> log.debug("忽略未知 op: {}", op);
    }
  }

  // ==================== 事件分发 ====================

  private void onDispatch(JsonNode node) {
    long seq = node.path("s").asLong(0);
    if (seq > 0) {
      synchronized (connectionLock) {
        if (seq > lastSeq) {
          lastSeq = seq;
        }
      }
    }

    String type = node.path("t").asText("");
    switch (type) {
      case "READY" -> {
        JsonNode data = node.path("d");
        String newSessionId = data.path("session_id").asText("");
        synchronized (connectionLock) {
          sessionId = newSessionId.isBlank() ? null : newSessionId;
        }
        ready = true;
        log.info("QQ 机器人已上线: {}", data.path("user").path("username").asText(""));
      }
      case "RESUMED" -> {
        ready = true;
        log.info("QQ WebSocket 会话已恢复");
      }
      default -> {
        QqIncomingMessage message = EventMapper.map(node);
        if (message == null) {
          return;
        }
        if (!deduplicator.firstSeen(message.eventId())) {
          log.debug("忽略重复事件: {}", message.eventId());
          return;
        }
        eventExecutor.execute(() -> deliver(message));
      }
    }
  }

  private void deliver(QqIncomingMessage message) {
    try {
      listener.onMessage(message);
    } catch (Exception e) {
      log.error("处理 QQ 消息事件失败: eventId={}, scene={}", message.eventId(), message.scene(), e);
    }
  }

  // ==================== 心跳与鉴权 ====================

  private void startHeartbeat(WsTransport connection, long intervalMillis) {
    stopHeartbeat();
    heartbeatAckPending = false;
    long interval = Math.max(5_000, intervalMillis);
    ScheduledFuture<?> task =
        scheduler.scheduleAtFixedRate(
            () -> {
              try {
                if (heartbeatAckPending) {
                  log.warn("心跳未收到 ACK，主动重连");
                  closeTransportIfCurrent(connection);
                  return;
                }
                heartbeatAckPending = true;
                sendHeartbeat(connection);
              } catch (RuntimeException e) {
                log.warn("心跳发送失败，主动重连: {}", e.toString());
                closeTransportIfCurrent(connection);
              }
            },
            interval,
            interval,
            TimeUnit.MILLISECONDS);
    synchronized (connectionLock) {
      heartbeatTask = task;
    }
  }

  private void stopHeartbeat() {
    ScheduledFuture<?> task;
    synchronized (connectionLock) {
      task = heartbeatTask;
      heartbeatTask = null;
    }
    if (task != null) {
      task.cancel(false);
    }
  }

  private void sendHeartbeat(WsTransport connection) {
    long seq;
    synchronized (connectionLock) {
      seq = lastSeq;
    }
    ObjectNode payload = mapper.createObjectNode();
    payload.put("op", OP_HEARTBEAT);
    if (seq > 0) {
      payload.put("d", seq);
    } else {
      payload.putNull("d");
    }
    connection.sendText(payload.toString());
  }

  private void sendIdentifyOrResume(WsTransport connection) {
    QqGatewayConfig config = client.config();
    String wsToken =
        switch (config.wsTokenMode()) {
          case ACCESS_TOKEN -> "QQBot " + client.tokens().token();
          case APP_TICKET -> "Bot " + config.appId() + "." + config.appToken();
        };

    ObjectNode data = mapper.createObjectNode();
    data.put("token", wsToken);

    String currentSession;
    long resumeSeq;
    synchronized (connectionLock) {
      currentSession = sessionId;
      resumeSeq = lastSeq;
    }

    if (currentSession != null) {
      data.put("session_id", currentSession);
      data.put("seq", resumeSeq);
      send(connection, OP_RESUME, data);
      log.info("已发送 Resume: session={}, seq={}", currentSession, resumeSeq);
      return;
    }

    data.put("intents", config.intents());
    var shard = data.putArray("shard");
    shard.add(0);
    shard.add(1);
    ObjectNode properties = data.putObject("properties");
    properties.put("$os", System.getProperty("os.name", "unknown"));
    properties.put("$browser", "xiantao-qq-gateway");
    properties.put("$device", "xiantao-qq-gateway");
    send(connection, OP_IDENTIFY, data);
    log.info("已发送 Identify: intents={}", config.intents());
  }

  private void send(WsTransport connection, int op, ObjectNode data) {
    ObjectNode payload = mapper.createObjectNode();
    payload.put("op", op);
    payload.set("d", data);
    connection.sendText(payload.toString());
  }

  /** 与当前连接对象做同一性比较（故意使用引用相等）。 */
  @SuppressWarnings("ReferenceEquality")
  private void clearTransportIfCurrent(WsTransport connection) {
    synchronized (connectionLock) {
      if (transport == connection) {
        transport = null;
      }
    }
  }

  /** 仅当指定连接仍是当前连接时关闭它，避免迟到的任务误杀新会话。 */
  @SuppressWarnings("ReferenceEquality")
  private void closeTransportIfCurrent(WsTransport connection) {
    boolean current;
    synchronized (connectionLock) {
      current = transport == connection;
    }
    if (current) {
      connection.close();
    }
  }

  /** 连接建立后若迟迟未完成鉴权（READY/RESUMED），主动断开重连。 */
  private void scheduleAuthWatchdog(WsTransport connection) {
    if (authTimeout.isZero() || authTimeout.isNegative()) {
      return;
    }
    ScheduledFuture<?> task =
        scheduler.schedule(
            () -> {
              if (!ready) {
                log.warn("QQ WebSocket 鉴权超时（{}ms 内未收到 READY/RESUMED），主动重连", authTimeout.toMillis());
                closeTransportIfCurrent(connection);
              }
            },
            authTimeout.toMillis(),
            TimeUnit.MILLISECONDS);
    synchronized (connectionLock) {
      authWatchdogTask = task;
    }
  }

  private void cancelAuthWatchdog() {
    ScheduledFuture<?> task;
    synchronized (connectionLock) {
      task = authWatchdogTask;
      authWatchdogTask = null;
    }
    if (task != null) {
      task.cancel(false);
    }
  }

  private void closeTransport() {
    WsTransport current;
    synchronized (connectionLock) {
      current = transport;
    }
    if (current != null) {
      current.close();
    }
  }

  private static Duration min(Duration duration) {
    return duration.compareTo(MAX_BACKOFF) > 0 ? MAX_BACKOFF : duration;
  }

  private static String abbreviate(String text) {
    return text.length() <= 200 ? text : text.substring(0, 200) + "...";
  }
}
