package top.stillmisty.xiantao.handle.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqScene;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.NextActions;
import top.stillmisty.xiantao.handle.platform.PlatformHandler;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;

class ReplyHelperTest {

  @Test
  void sendsCommandText() {
    StubHandler handler = new StubHandler();
    helper(handler).dispatch(message(), "状态", fmt -> "正常回复");

    assertEquals(List.of("正常回复"), handler.replies);
  }

  @Test
  void replacesThrownExceptionWithErrorText() {
    StubHandler handler = new StubHandler();
    helper(handler)
        .dispatch(
            message(),
            "异常命令",
            fmt -> {
              throw new IllegalStateException("boom");
            });

    assertTrue(handler.replies.getFirst().contains("系统繁忙"), handler.replies.getFirst());
  }

  @Test
  @SuppressWarnings("NullAway")
  void replacesNullTextWithErrorText() {
    StubHandler handler = new StubHandler();
    helper(handler).dispatch(message(), "空回复", fmt -> null);

    assertTrue(handler.replies.getFirst().contains("系统繁忙"), handler.replies.getFirst());
  }

  @Test
  void replacesBlankTextWithErrorText() {
    StubHandler handler = new StubHandler();
    helper(handler).dispatch(message(), "空白回复", fmt -> "   ");

    assertTrue(handler.replies.getFirst().contains("系统繁忙"), handler.replies.getFirst());
  }

  @Test
  void passesCollectedSuggestionsToPlatform() {
    StubHandler handler = new StubHandler();
    helper(handler)
        .dispatch(
            message(),
            "地图",
            fmt -> {
              NextActions.suggest("翠竹林", "前往 翠竹林");
              return "地图正文";
            });

    assertEquals(
        List.of(new NextActions.Suggestion("翠竹林", "前往 翠竹林")), handler.suggestions.getFirst());
  }

  private static ReplyHelper helper(PlatformHandler handler) {
    return new ReplyHelper(new PlatformRegistry(List.of(handler)));
  }

  private static QqIncomingMessage message() {
    return new QqIncomingMessage(
        "EVENT-1", "MSG-1", QqScene.GROUP_AT, "OPEN-1", "GROUP-1", "状态", null, Instant.EPOCH);
  }

  private static final class StubHandler implements PlatformHandler {

    final List<String> replies = new CopyOnWriteArrayList<>();
    final List<List<NextActions.Suggestion>> suggestions = new CopyOnWriteArrayList<>();

    @Override
    public PlatformType getPlatformType() {
      return PlatformType.QQ;
    }

    @Override
    public boolean supports(QqIncomingMessage message) {
      return true;
    }

    @Override
    public String extractOpenId(QqIncomingMessage message) {
      return message.openId();
    }

    @Override
    public void replyText(QqIncomingMessage message, String text) {
      replies.add(text);
    }

    @Override
    public void replyText(
        QqIncomingMessage message, String text, List<NextActions.Suggestion> nextActions) {
      replies.add(text);
      suggestions.add(nextActions);
    }
  }
}
