package top.stillmisty.xiantao.handle.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import top.stillmisty.qqgateway.QqButton;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.qqgateway.QqScene;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.NextActions;
import top.stillmisty.xiantao.service.NotificationAppender;

class ReplyDeliveryTest {

  @Test
  void passesChoiceKeyboardWhenEnabled() {
    QqKeyboard keyboard = choiceKeyboard();
    NotificationAppender appender = appenderReturning("正文", List.of(), keyboard);
    CapturingHandler handler = new CapturingHandler(true, true);

    new ReplyDelivery(appender).deliver(handler, message(), "正文", List.of());

    assertEquals("正文", handler.segments.getFirst());
    assertEquals(keyboard, handler.keyboard);
  }

  @Test
  void omitsChoiceKeyboardWhenDisabled() {
    NotificationAppender appender = appenderReturning("正文", List.of(), choiceKeyboard());
    CapturingHandler handler = new CapturingHandler(false, true);

    new ReplyDelivery(appender).deliver(handler, message(), "正文", List.of());

    assertNull(handler.keyboard);
  }

  @Test
  void buildsSuggestionKeyboardWhenNoChoiceEvent() {
    NotificationAppender appender = appenderReturning("正文", List.of(), null);
    CapturingHandler handler = new CapturingHandler(true, true);
    List<NextActions.Suggestion> suggestions =
        List.of(
            new NextActions.Suggestion("翠竹林", "前往 翠竹林"),
            new NextActions.Suggestion("黑风岭", "前往 黑风岭"));

    new ReplyDelivery(appender).deliver(handler, message(), "正文", suggestions);

    QqKeyboard keyboard = Objects.requireNonNull(handler.keyboard);
    assertEquals(2, keyboard.buttonCount());
    assertEquals("翠竹林", keyboard.rows().getFirst().buttons().getFirst().label());
    assertEquals("前往 翠竹林", keyboard.rows().getFirst().buttons().getFirst().data());
    assertEquals("前往 黑风岭", keyboard.rows().getFirst().buttons().getLast().data());
  }

  @Test
  void suggestionLabelIsTruncatedToPlatformLimit() {
    NotificationAppender appender = appenderReturning("正文", List.of(), null);
    CapturingHandler handler = new CapturingHandler(true, true);
    String longName = "极长的地区名称测试用例甲乙丙丁戊己庚辛";

    new ReplyDelivery(appender)
        .deliver(
            handler,
            message(),
            "正文",
            List.of(new NextActions.Suggestion(longName, "前往 " + longName)));

    String label =
        Objects.requireNonNull(handler.keyboard).rows().getFirst().buttons().getFirst().label();
    assertTrue(label.codePointCount(0, label.length()) <= 10, label);
  }

  @Test
  void choiceKeyboardWinsOverSuggestions() {
    QqKeyboard keyboard = choiceKeyboard();
    NotificationAppender appender = appenderReturning("正文", List.of(), keyboard);
    CapturingHandler handler = new CapturingHandler(true, true);

    new ReplyDelivery(appender)
        .deliver(handler, message(), "正文", List.of(new NextActions.Suggestion("翠竹林", "前往 翠竹林")));

    assertEquals(keyboard, handler.keyboard);
  }

  @Test
  void omitsSuggestionKeyboardWhenDisabled() {
    NotificationAppender appender = appenderReturning("正文", List.of(), null);
    CapturingHandler handler = new CapturingHandler(true, false);

    new ReplyDelivery(appender)
        .deliver(handler, message(), "正文", List.of(new NextActions.Suggestion("翠竹林", "前往 翠竹林")));

    assertNull(handler.keyboard);
  }

  @Test
  void longReplyIsSplitIntoSegments() {
    String text = "测".repeat(1000);
    NotificationAppender appender = appenderReturning(text, List.of(), null);
    CapturingHandler handler = new CapturingHandler(true, true);

    new ReplyDelivery(appender).deliver(handler, message(), text, List.of());

    assertEquals(2, handler.segments.size());
    assertEquals(text, String.join("", handler.segments));
  }

  @Test
  void marksDeliveredOnlyAfterSending() {
    NotificationAppender appender = appenderReturning("正文", List.of(7L), null);
    PlatformHandler handler = mock(PlatformHandler.class);
    when(handler.getPlatformType()).thenReturn(PlatformType.QQ);
    when(handler.extractOpenId(any())).thenReturn("OPEN-1");
    when(handler.replyLimits()).thenReturn(new ReplyLimits(1800, 4, 10, true, true));

    new ReplyDelivery(appender).deliver(handler, message(), "正文", List.of());

    InOrder order = inOrder(handler, appender);
    order.verify(handler).sendReply(any(), any(), any());
    order.verify(appender).markDelivered(List.of(7L));
  }

  private static NotificationAppender appenderReturning(
      String text, List<Long> eventIds, @Nullable QqKeyboard keyboard) {
    NotificationAppender appender = mock(NotificationAppender.class);
    when(appender.prepareAppend(eq(PlatformType.QQ), eq("OPEN-1"), any(), any()))
        .thenReturn(new NotificationAppender.AppendResult(text, eventIds, keyboard));
    return appender;
  }

  private static QqKeyboard choiceKeyboard() {
    return QqKeyboard.commandGrid(List.of(QqButton.command("choice-0", "进入洞穴", "选 A")));
  }

  private static QqIncomingMessage message() {
    return new QqIncomingMessage(
        "EVENT-1", "MSG-1", QqScene.GROUP_AT, "OPEN-1", "GROUP-1", "状态", null, Instant.EPOCH);
  }

  private static final class CapturingHandler implements PlatformHandler {

    final List<List<String>> sends = new ArrayList<>();
    final List<String> segments = new ArrayList<>();
    @Nullable QqKeyboard keyboard;
    private final boolean choiceButtons;
    private final boolean suggestionButtons;

    CapturingHandler(boolean choiceButtons, boolean suggestionButtons) {
      this.choiceButtons = choiceButtons;
      this.suggestionButtons = suggestionButtons;
    }

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
    public ReplyLimits replyLimits() {
      return new ReplyLimits(1800, 4, 10, choiceButtons, suggestionButtons);
    }

    @Override
    public void sendReply(
        QqIncomingMessage message, List<String> segments, @Nullable QqKeyboard keyboard) {
      this.segments.clear();
      this.segments.addAll(segments);
      this.sends.add(List.copyOf(segments));
      this.keyboard = keyboard;
    }
  }
}
