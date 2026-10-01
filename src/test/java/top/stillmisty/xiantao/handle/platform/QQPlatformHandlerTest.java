package top.stillmisty.xiantao.handle.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import top.stillmisty.qqgateway.QqButton;
import top.stillmisty.qqgateway.QqGatewayConfig;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.qqgateway.QqMessageSender;
import top.stillmisty.qqgateway.QqScene;
import top.stillmisty.qqgateway.QqWebhookHandler;
import top.stillmisty.xiantao.config.QqProperties;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.NextActions;
import top.stillmisty.xiantao.service.NotificationAppender;

class QQPlatformHandlerTest {

  @Test
  void passesChoiceKeyboardWhenEnabled() {
    QqKeyboard keyboard = choiceKeyboard();
    NotificationAppender appender = appenderReturning(keyboard);
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();

    handler(appender, sender, true, true).replyText(message, "正文");

    verify(sender).replyMarkdown(message, "正文", keyboard);
  }

  @Test
  void omitsChoiceKeyboardWhenDisabled() {
    NotificationAppender appender = appenderReturning(choiceKeyboard());
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();

    handler(appender, sender, false, true).replyText(message, "正文");

    verify(sender).replyMarkdown(eq(message), eq("正文"), isNull());
  }

  @Test
  void buildsSuggestionKeyboardWhenNoChoiceEvent() {
    NotificationAppender appender = appenderReturning(null);
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();
    List<NextActions.Suggestion> suggestions =
        List.of(
            new NextActions.Suggestion("翠竹林", "前往 翠竹林"),
            new NextActions.Suggestion("黑风岭", "前往 黑风岭"));

    handler(appender, sender, true, true).replyText(message, "正文", suggestions);

    ArgumentCaptor<QqKeyboard> captor = ArgumentCaptor.forClass(QqKeyboard.class);
    verify(sender).replyMarkdown(eq(message), eq("正文"), captor.capture());
    QqKeyboard keyboard = captor.getValue();
    assertEquals(2, keyboard.buttonCount());
    assertEquals("翠竹林", keyboard.rows().getFirst().buttons().getFirst().label());
    assertEquals("前往 翠竹林", keyboard.rows().getFirst().buttons().getFirst().data());
    assertEquals("前往 黑风岭", keyboard.rows().getFirst().buttons().getLast().data());
  }

  @Test
  void choiceKeyboardWinsOverSuggestions() {
    QqKeyboard keyboard = choiceKeyboard();
    NotificationAppender appender = appenderReturning(keyboard);
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();

    handler(appender, sender, true, true)
        .replyText(message, "正文", List.of(new NextActions.Suggestion("翠竹林", "前往 翠竹林")));

    verify(sender).replyMarkdown(message, "正文", keyboard);
  }

  @Test
  void omitsSuggestionKeyboardWhenDisabled() {
    NotificationAppender appender = appenderReturning(null);
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();

    handler(appender, sender, true, false)
        .replyText(message, "正文", List.of(new NextActions.Suggestion("翠竹林", "前往 翠竹林")));

    verify(sender).replyMarkdown(eq(message), eq("正文"), isNull());
  }

  @Test
  void longReplyIsSplitIntoSegments() {
    NotificationAppender appender = appenderReturning(null);
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();
    String text = "测".repeat(1000);

    handler(appender, sender, true, true).replyText(message, text);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(sender, times(2)).replyMarkdown(eq(message), captor.capture(), isNull());
    assertEquals(text, String.join("", captor.getAllValues()));
  }

  private static NotificationAppender appenderReturning(@Nullable QqKeyboard keyboard) {
    NotificationAppender appender = mock(NotificationAppender.class);
    when(appender.prepareAppend(eq(PlatformType.QQ), eq("OPEN-1"), any(), any()))
        .thenAnswer(
            invocation ->
                new NotificationAppender.AppendResult(
                    invocation.getArgument(2), List.of(), keyboard));
    return appender;
  }

  @SuppressWarnings("unchecked")
  private static QQPlatformHandler handler(
      NotificationAppender appender,
      QqMessageSender sender,
      boolean choiceButtons,
      boolean suggestionButtons) {
    ObjectProvider<QqMessageSender> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(sender);
    return new QQPlatformHandler(appender, provider, properties(choiceButtons, suggestionButtons));
  }

  private static QqKeyboard choiceKeyboard() {
    return QqKeyboard.commandGrid(List.of(QqButton.command("choice-0", "进入洞穴", "选 A")));
  }

  private static QqProperties properties(boolean choiceButtons, boolean suggestionButtons) {
    return new QqProperties(
        true,
        "app-1",
        "secret-1",
        "",
        QqGatewayConfig.WsTokenMode.ACCESS_TOKEN,
        QqProperties.Transport.WEBSOCKET,
        "",
        "",
        QqGatewayConfig.INTENT_GROUP_AND_C2C,
        "/qq/webhook",
        QqWebhookHandler.DEFAULT_MAX_EVENT_AGE,
        choiceButtons,
        suggestionButtons,
        false);
  }

  private static QqIncomingMessage message() {
    return new QqIncomingMessage(
        "EVENT-1", "MSG-1", QqScene.GROUP_AT, "OPEN-1", "GROUP-1", "状态", null, Instant.EPOCH);
  }
}
