package top.stillmisty.qqgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class QqWebSocketGatewayTest {

  @Test
  void identifiesDispatchesAndDeduplicates() throws Exception {
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request ->
              switch (request.path()) {
                case "/app/getAppAccessToken" ->
                    TestHttpServer.Response.json(
                        "{\"access_token\":\"tok-1\",\"expires_in\":\"7200\"}");
                case "/gateway" ->
                    TestHttpServer.Response.json("{\"url\":\"ws://127.0.0.1:9/websocket\"}");
                default -> TestHttpServer.Response.status(404, "{}");
              });

      List<FakeTransport> transports = new CopyOnWriteArrayList<>();
      List<QqIncomingMessage> received = new CopyOnWriteArrayList<>();

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server));
          QqWebSocketGateway gateway =
              new QqWebSocketGateway(client, received::add, () -> newFakeTransport(transports))) {
        gateway.start();
        awaitTrue(() -> !transports.isEmpty());
        FakeTransport first = transports.getFirst();

        first.emit("{\"op\":10,\"d\":{\"heartbeat_interval\":60000}}");
        awaitTrue(() -> first.sent.stream().anyMatch(payload -> payload.contains("\"op\":2")));

        String identify =
            first.sent.stream()
                .filter(payload -> payload.contains("\"op\":2"))
                .findFirst()
                .orElseThrow();
        assertTrue(identify.contains("\"intents\":33554432"), identify);
        assertTrue(identify.contains("\"token\":\"QQBot tok-1\""), identify);
        assertTrue(identify.contains("\"shard\":[0,1]"), identify);

        first.emit(
            "{\"op\":0,\"s\":1,\"t\":\"READY\",\"d\":{\"session_id\":\"sess-1\","
                + "\"user\":{\"username\":\"bot\"}}}");
        awaitTrue(gateway::isConnected);

        first.emit(QqTestSupport.groupAtEventJson());
        awaitTrue(() -> received.size() == 1);
        assertEquals("MEMBER-1", received.getFirst().openId());
        assertEquals("GROUP-1", received.getFirst().groupOpenId());

        first.emit(QqTestSupport.groupAtEventJson());
        Thread.sleep(150);
        assertEquals(1, received.size(), "重复事件应被去重");
      }
    }
  }

  @Test
  void reconnectsWithResumeAfterConnectionDrop() throws Exception {
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request ->
              switch (request.path()) {
                case "/app/getAppAccessToken" ->
                    TestHttpServer.Response.json(
                        "{\"access_token\":\"tok-1\",\"expires_in\":\"7200\"}");
                case "/gateway" ->
                    TestHttpServer.Response.json("{\"url\":\"ws://127.0.0.1:9/websocket\"}");
                default -> TestHttpServer.Response.status(404, "{}");
              });

      List<FakeTransport> transports = new CopyOnWriteArrayList<>();
      List<QqIncomingMessage> received = new CopyOnWriteArrayList<>();

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server));
          QqWebSocketGateway gateway =
              new QqWebSocketGateway(client, received::add, () -> newFakeTransport(transports))) {
        gateway.start();
        awaitTrue(() -> !transports.isEmpty());
        FakeTransport first = transports.getFirst();
        first.emit("{\"op\":10,\"d\":{\"heartbeat_interval\":60000}}");
        first.emit(
            "{\"op\":0,\"s\":1,\"t\":\"READY\",\"d\":{\"session_id\":\"sess-1\","
                + "\"user\":{\"username\":\"bot\"}}}");
        awaitTrue(gateway::isConnected);

        first.disconnect();
        awaitTrue(() -> transports.size() >= 2);
        FakeTransport second = transports.get(1);
        second.emit("{\"op\":10,\"d\":{\"heartbeat_interval\":60000}}");
        awaitTrue(() -> second.sent.stream().anyMatch(payload -> payload.contains("\"op\":6")));

        String resume =
            second.sent.stream()
                .filter(payload -> payload.contains("\"op\":6"))
                .findFirst()
                .orElseThrow();
        assertTrue(resume.contains("\"session_id\":\"sess-1\""), resume);
        assertTrue(resume.contains("\"seq\":1"), resume);
        assertTrue(resume.contains("\"token\":\"QQBot tok-1\""), resume);
      }
    }
  }

  @Test
  void reconnectsWhenIdentifySendFails() throws Exception {
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request ->
              switch (request.path()) {
                case "/app/getAppAccessToken" ->
                    TestHttpServer.Response.json(
                        "{\"access_token\":\"tok-1\",\"expires_in\":\"7200\"}");
                case "/gateway" ->
                    TestHttpServer.Response.json("{\"url\":\"ws://127.0.0.1:9/websocket\"}");
                default -> TestHttpServer.Response.status(404, "{}");
              });

      List<FakeTransport> transports = new CopyOnWriteArrayList<>();
      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server));
          QqWebSocketGateway gateway =
              new QqWebSocketGateway(client, message -> {}, () -> newFakeTransport(transports))) {
        gateway.start();
        awaitTrue(() -> !transports.isEmpty());
        FakeTransport first = transports.getFirst();
        first.failIdentify();
        first.emit("{\"op\":10,\"d\":{\"heartbeat_interval\":60000}}");

        // Identify 发送失败应主动断开并重连，而不是让连接空转
        awaitTrue(() -> transports.size() >= 2);
      }
    }
  }

  @Test
  void reconnectsWhenAuthenticationTimesOut() throws Exception {
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request ->
              switch (request.path()) {
                case "/app/getAppAccessToken" ->
                    TestHttpServer.Response.json(
                        "{\"access_token\":\"tok-1\",\"expires_in\":\"7200\"}");
                case "/gateway" ->
                    TestHttpServer.Response.json("{\"url\":\"ws://127.0.0.1:9/websocket\"}");
                default -> TestHttpServer.Response.status(404, "{}");
              });

      List<FakeTransport> transports = new CopyOnWriteArrayList<>();
      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server));
          QqWebSocketGateway gateway =
              new QqWebSocketGateway(
                  client,
                  message -> {},
                  () -> newFakeTransport(transports),
                  java.time.Duration.ofMillis(200))) {
        gateway.start();
        awaitTrue(() -> !transports.isEmpty());
        // 平台不下发 HELLO：看门狗应在超时后断开并重连
        awaitTrue(() -> transports.size() >= 2);
      }
    }
  }

  private static FakeTransport newFakeTransport(List<FakeTransport> transports) {
    FakeTransport transport = new FakeTransport();
    transports.add(transport);
    return transport;
  }

  private static void awaitTrue(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail("等待条件超时");
      }
      Thread.sleep(20);
    }
  }

  private static final class FakeTransport implements WsTransport {

    private final List<String> sent = new CopyOnWriteArrayList<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private volatile @Nullable Listener listener;
    private volatile boolean failIdentify;

    @Override
    public void connect(URI uri, Listener listener) {
      this.listener = listener;
      listener.onOpen();
    }

    @Override
    public void sendText(String payload) {
      if (failIdentify && payload.contains("\"op\":2")) {
        throw new IllegalStateException("模拟 Identify 发送失败");
      }
      sent.add(payload);
    }

    @Override
    public void close() {
      if (closed.compareAndSet(false, true)) {
        Listener current = listener;
        if (current != null) {
          current.onClosed(1000, "closed");
        }
      }
    }

    /** 模拟平台推送一条消息。 */
    void emit(String payload) {
      Listener current = listener;
      if (current != null) {
        current.onText(payload);
      }
    }

    /** 模拟连接被平台/网络断开。 */
    void disconnect() {
      close();
    }

    /** 让 Identify 发送失败。 */
    void failIdentify() {
      failIdentify = true;
    }
  }
}
