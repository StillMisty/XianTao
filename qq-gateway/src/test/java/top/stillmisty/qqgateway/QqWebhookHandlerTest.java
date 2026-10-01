package top.stillmisty.qqgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class QqWebhookHandlerTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String SECRET = "secret-1";

  @Test
  void answersValidationChallenge() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server));
        QqWebhookHandler handler = new QqWebhookHandler(client, message -> {})) {
      byte[] body =
          "{\"op\":13,\"d\":{\"plain_token\":\"pt-1\",\"event_ts\":\"1725442341\"}}"
              .getBytes(StandardCharsets.UTF_8);

      QqWebhookHandler.WebhookResult result = handler.handle(body, null);

      assertEquals(200, result.status());
      JsonNode response = MAPPER.readTree(result.body());
      assertEquals("pt-1", response.path("plain_token").asText());
      assertEquals(
          new QqWebhookVerifier(SECRET).signChallenge("pt-1", "1725442341"),
          response.path("signature").asText());
    }
  }

  @Test
  void acceptsSignedEventAndDeduplicates() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
      CopyOnWriteArrayList<QqIncomingMessage> received = new CopyOnWriteArrayList<>();
      try (QqWebhookHandler handler = new QqWebhookHandler(client, received::add)) {
        byte[] body = QqTestSupport.groupAtEventJson().getBytes(StandardCharsets.UTF_8);
        String signature = new QqWebhookVerifier(SECRET).signHex(body);

        QqWebhookHandler.WebhookResult result = handler.handle(body, signature);
        assertEquals(200, result.status());
        assertTrue(result.body().contains("\"op\":12"), result.body());

        awaitTrue(() -> received.size() == 1);
        assertEquals("MEMBER-1", received.getFirst().openId());

        handler.handle(body, signature);
        Thread.sleep(150);
        assertEquals(1, received.size(), "重复事件应被去重");
      }
    }
  }

  @Test
  void rejectsInvalidSignature() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server));
        QqWebhookHandler handler = new QqWebhookHandler(client, message -> {})) {
      byte[] body = QqTestSupport.groupAtEventJson().getBytes(StandardCharsets.UTF_8);

      assertEquals(401, handler.handle(body, "00").status());
      assertEquals(401, handler.handle(body, null).status());
    }
  }

  @Test
  void rejectsMalformedJson() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server));
        QqWebhookHandler handler = new QqWebhookHandler(client, message -> {})) {
      assertEquals(400, handler.handle("not-json".getBytes(StandardCharsets.UTF_8), null).status());
    }
  }

  @Test
  void rejectsStaleEvent() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
      CopyOnWriteArrayList<QqIncomingMessage> received = new CopyOnWriteArrayList<>();
      try (QqWebhookHandler handler = new QqWebhookHandler(client, received::add)) {
        byte[] body =
            QqTestSupport.groupAtEventJson(Instant.now().minus(1, ChronoUnit.HOURS))
                .getBytes(StandardCharsets.UTF_8);
        String signature = new QqWebhookVerifier(SECRET).signHex(body);

        assertEquals(401, handler.handle(body, signature).status());
        Thread.sleep(150);
        assertTrue(received.isEmpty(), "过期事件不应被处理");
      }
    }
  }

  @Test
  void rejectsFarFutureEvent() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
      CopyOnWriteArrayList<QqIncomingMessage> received = new CopyOnWriteArrayList<>();
      try (QqWebhookHandler handler = new QqWebhookHandler(client, received::add)) {
        byte[] body =
            QqTestSupport.groupAtEventJson(Instant.now().plus(2, ChronoUnit.HOURS))
                .getBytes(StandardCharsets.UTF_8);
        String signature = new QqWebhookVerifier(SECRET).signHex(body);

        assertEquals(401, handler.handle(body, signature).status());
        Thread.sleep(150);
        assertTrue(received.isEmpty(), "未来时间戳事件不应被处理");
      }
    }
  }

  @Test
  void acceptsEventWithoutTimestamp() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
      CopyOnWriteArrayList<QqIncomingMessage> received = new CopyOnWriteArrayList<>();
      try (QqWebhookHandler handler = new QqWebhookHandler(client, received::add)) {
        byte[] body =
            QqTestSupport.groupAtEventJsonWithoutTimestamp().getBytes(StandardCharsets.UTF_8);
        String signature = new QqWebhookVerifier(SECRET).signHex(body);

        assertEquals(200, handler.handle(body, signature).status());
        awaitTrue(() -> received.size() == 1);
      }
    }
  }

  @Test
  void freshnessCheckCanBeDisabled() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
      CopyOnWriteArrayList<QqIncomingMessage> received = new CopyOnWriteArrayList<>();
      try (QqWebhookHandler handler =
          new QqWebhookHandler(client, received::add, java.time.Duration.ZERO)) {
        byte[] body =
            QqTestSupport.groupAtEventJson(Instant.now().minus(30, ChronoUnit.DAYS))
                .getBytes(StandardCharsets.UTF_8);
        String signature = new QqWebhookVerifier(SECRET).signHex(body);

        assertEquals(200, handler.handle(body, signature).status());
        awaitTrue(() -> received.size() == 1);
      }
    }
  }

  @Test
  void validationChallengeIgnoresFreshness() throws Exception {
    try (TestHttpServer server = new TestHttpServer();
        QqBotClient client = new QqBotClient(QqTestSupport.config(server));
        QqWebhookHandler handler = new QqWebhookHandler(client, message -> {})) {
      byte[] body =
          "{\"op\":13,\"d\":{\"plain_token\":\"pt\",\"event_ts\":\"1000000000\"}}"
              .getBytes(StandardCharsets.UTF_8);

      assertEquals(200, handler.handle(body, null).status());
    }
  }

  private static void awaitTrue(BooleanSupplier condition) throws InterruptedException {
    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) {
        fail("等待条件超时");
      }
      Thread.sleep(20);
    }
  }
}
