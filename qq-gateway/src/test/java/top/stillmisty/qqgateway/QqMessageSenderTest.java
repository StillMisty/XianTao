package top.stillmisty.qqgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class QqMessageSenderTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String GROUP_MESSAGES_PATH = "/v2/groups/GROUP-1/messages";
  private static final String C2C_MESSAGES_PATH = "/v2/users/MEMBER-1/messages";

  @Test
  void replySendsMarkdownWithIncrementingSeqAndCachedToken() throws Exception {
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request ->
              switch (request.path()) {
                case "/app/getAppAccessToken" ->
                    TestHttpServer.Response.json(
                        "{\"access_token\":\"tok-1\",\"expires_in\":\"7200\"}");
                case GROUP_MESSAGES_PATH -> TestHttpServer.Response.json("{\"id\":\"sent-1\"}");
                default -> TestHttpServer.Response.status(404, "{\"code\":404}");
              });

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "**hello**");
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "again");
      }

      List<TestHttpServer.Request> sends =
          server.requests().stream()
              .filter(request -> request.path().equals(GROUP_MESSAGES_PATH))
              .toList();
      assertEquals(2, sends.size());
      assertEquals("QQBot tok-1", sends.getFirst().header("Authorization"));

      JsonNode first = MAPPER.readTree(sends.getFirst().body());
      assertEquals(2, first.path("msg_type").asInt());
      assertEquals("MSG-1", first.path("msg_id").asText());
      assertEquals(1, first.path("msg_seq").asInt());
      assertEquals("**hello**", first.path("markdown").path("content").asText());

      JsonNode second = MAPPER.readTree(sends.get(1).body());
      assertEquals(2, second.path("msg_seq").asInt());

      assertEquals(1, server.countRequests("/app/getAppAccessToken"));
    }
  }

  @Test
  void refreshesTokenOn401AndRetriesOnce() throws Exception {
    AtomicInteger tokenCalls = new AtomicInteger();
    AtomicInteger messageCalls = new AtomicInteger();
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request -> {
            if (request.path().equals("/app/getAppAccessToken")) {
              return TestHttpServer.Response.json(
                  "{\"access_token\":\"tok-"
                      + tokenCalls.incrementAndGet()
                      + "\",\"expires_in\":\"7200\"}");
            }
            if (request.path().equals(GROUP_MESSAGES_PATH)) {
              if (messageCalls.incrementAndGet() == 1) {
                return TestHttpServer.Response.status(
                    401, "{\"code\":11242,\"message\":\"token expired\"}");
              }
              return TestHttpServer.Response.json("{\"id\":\"sent\"}");
            }
            return TestHttpServer.Response.status(404, "{}");
          });

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "hi");
      }

      assertEquals(2, tokenCalls.get());
      assertEquals(2, messageCalls.get());
      List<TestHttpServer.Request> sends =
          server.requests().stream()
              .filter(request -> request.path().equals(GROUP_MESSAGES_PATH))
              .toList();
      assertEquals("QQBot tok-1", sends.getFirst().header("Authorization"));
      assertEquals("QQBot tok-2", sends.get(1).header("Authorization"));
    }
  }

  @Test
  void retriesOn429AndKeepsSameSeq() throws Exception {
    AtomicInteger messageCalls = new AtomicInteger();
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request -> {
            if (request.path().equals("/app/getAppAccessToken")) {
              return TestHttpServer.Response.json(
                  "{\"access_token\":\"tok\",\"expires_in\":\"7200\"}");
            }
            if (request.path().equals(GROUP_MESSAGES_PATH)) {
              if (messageCalls.incrementAndGet() == 1) {
                return TestHttpServer.Response.status(429, "{\"code\":11244}");
              }
              return TestHttpServer.Response.json("{\"id\":\"sent\"}");
            }
            return TestHttpServer.Response.status(404, "{}");
          });

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "hi");
      }

      List<TestHttpServer.Request> sends =
          server.requests().stream()
              .filter(request -> request.path().equals(GROUP_MESSAGES_PATH))
              .toList();
      assertEquals(2, sends.size());
      JsonNode retried = MAPPER.readTree(sends.get(1).body());
      assertEquals(1, retried.path("msg_seq").asInt());
      assertTrue(retried.path("msg_id").asText().equals("MSG-1"));
    }
  }

  @Test
  void c2cUsesUsersPathWithoutMsgId() throws Exception {
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request ->
              switch (request.path()) {
                case "/app/getAppAccessToken" ->
                    TestHttpServer.Response.json(
                        "{\"access_token\":\"tok\",\"expires_in\":\"7200\"}");
                case C2C_MESSAGES_PATH -> TestHttpServer.Response.json("{\"id\":\"sent\"}");
                default -> TestHttpServer.Response.status(404, "{}");
              });

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().sendC2CMarkdown("MEMBER-1", "hello");
      }

      TestHttpServer.Request send =
          server.requests().stream()
              .filter(request -> request.path().equals(C2C_MESSAGES_PATH))
              .findFirst()
              .orElseThrow();
      JsonNode body = MAPPER.readTree(send.body());
      assertEquals(2, body.path("msg_type").asInt());
      assertTrue(body.path("msg_id").isMissingNode());
      assertEquals("hello", body.path("markdown").path("content").asText());
    }
  }

  @Test
  void retriesOn5xx() throws Exception {
    AtomicInteger messageCalls = new AtomicInteger();
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request -> {
            if (request.path().equals("/app/getAppAccessToken")) {
              return TestHttpServer.Response.json(
                  "{\"access_token\":\"tok\",\"expires_in\":\"7200\"}");
            }
            if (request.path().equals(GROUP_MESSAGES_PATH)) {
              if (messageCalls.incrementAndGet() == 1) {
                return TestHttpServer.Response.status(502, "");
              }
              return TestHttpServer.Response.json("{\"id\":\"sent\"}");
            }
            return TestHttpServer.Response.status(404, "{}");
          });

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "hi");
      }

      assertEquals(2, messageCalls.get());
    }
  }

  @Test
  void staleTokenStillUsableWhenRefreshFails() throws Exception {
    AtomicInteger tokenCalls = new AtomicInteger();
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request -> {
            if (request.path().equals("/app/getAppAccessToken")) {
              if (tokenCalls.incrementAndGet() == 1) {
                // 有效期恰好等于刷新窗口：下一次调用必然触发刷新
                return TestHttpServer.Response.json(
                    "{\"access_token\":\"tok-old\",\"expires_in\":\"300\"}");
              }
              return TestHttpServer.Response.status(500, "");
            }
            if (request.path().equals(GROUP_MESSAGES_PATH)) {
              return TestHttpServer.Response.json("{\"id\":\"sent\"}");
            }
            return TestHttpServer.Response.status(404, "{}");
          });

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "first");
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "second");
      }

      assertEquals(2, tokenCalls.get(), "第二次应尝试刷新并失败");
      List<TestHttpServer.Request> sends =
          server.requests().stream()
              .filter(request -> request.path().equals(GROUP_MESSAGES_PATH))
              .toList();
      assertEquals(2, sends.size());
      assertEquals("QQBot tok-old", sends.get(1).header("Authorization"), "刷新失败时应回退到未过期的旧 token");
    }
  }

  @Test
  void replyWithKeyboardIncludesKeyboardJson() throws Exception {
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request ->
              switch (request.path()) {
                case "/app/getAppAccessToken" ->
                    TestHttpServer.Response.json(
                        "{\"access_token\":\"tok\",\"expires_in\":\"7200\"}");
                case GROUP_MESSAGES_PATH -> TestHttpServer.Response.json("{\"id\":\"sent\"}");
                default -> TestHttpServer.Response.status(404, "{}");
              });

      QqKeyboard keyboard =
          QqKeyboard.commandGrid(
              List.of(
                  QqButton.command("choice-0", "进入洞穴", "选 A"),
                  QqButton.command("choice-1", "转身离开", "选 B")));

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "正文", keyboard);
      }

      TestHttpServer.Request send =
          server.requests().stream()
              .filter(request -> request.path().equals(GROUP_MESSAGES_PATH))
              .findFirst()
              .orElseThrow();
      JsonNode button =
          MAPPER
              .readTree(send.body())
              .path("keyboard")
              .path("content")
              .path("rows")
              .get(0)
              .path("buttons")
              .get(0);

      assertEquals("choice-0", button.path("id").asText());
      assertEquals("进入洞穴", button.path("render_data").path("label").asText());
      assertEquals("进入洞穴", button.path("render_data").path("visited_label").asText());
      assertEquals(QqButton.STYLE_BLUE, button.path("render_data").path("style").asInt());
      assertEquals(QqButton.ACTION_COMMAND, button.path("action").path("type").asInt());
      assertEquals("选 A", button.path("action").path("data").asText());
      assertEquals(2, button.path("action").path("permission").path("type").asInt());
      assertTrue(button.path("action").path("enter").asBoolean());
      assertTrue(button.path("action").path("unsupport_tips").asText().contains("选 A"));
    }
  }

  @Test
  void degradesToPlainReplyWhenKeyboardRejected() throws Exception {
    AtomicInteger messageCalls = new AtomicInteger();
    try (TestHttpServer server = new TestHttpServer()) {
      server.setHandler(
          request -> {
            if (request.path().equals("/app/getAppAccessToken")) {
              return TestHttpServer.Response.json(
                  "{\"access_token\":\"tok\",\"expires_in\":\"7200\"}");
            }
            if (request.path().equals(GROUP_MESSAGES_PATH)) {
              if (messageCalls.incrementAndGet() == 1) {
                // 机器人未开通自定义按钮能力时，平台可能拒绝携带 keyboard 的请求
                return TestHttpServer.Response.status(
                    400, "{\"code\":40054005,\"message\":\"keyboard not supported\"}");
              }
              return TestHttpServer.Response.json("{\"id\":\"sent\"}");
            }
            return TestHttpServer.Response.status(404, "{}");
          });

      QqKeyboard keyboard =
          QqKeyboard.commandGrid(List.of(QqButton.command("choice-0", "进入洞穴", "选 A")));

      try (QqBotClient client = new QqBotClient(QqTestSupport.config(server))) {
        client.sender().replyMarkdown(QqTestSupport.groupMessage(), "正文", keyboard);
      }

      List<TestHttpServer.Request> sends =
          server.requests().stream()
              .filter(request -> request.path().equals(GROUP_MESSAGES_PATH))
              .toList();
      assertEquals(2, sends.size(), "应在按钮被拒后降级重试一次");
      assertTrue(MAPPER.readTree(sends.get(0).body()).has("keyboard"));
      assertTrue(MAPPER.readTree(sends.get(1).body()).path("keyboard").isMissingNode());
      assertEquals(
          MAPPER.readTree(sends.get(0).body()).path("msg_seq").asInt(),
          MAPPER.readTree(sends.get(1).body()).path("msg_seq").asInt(),
          "降级重试应复用同一序号");
    }
  }
}
