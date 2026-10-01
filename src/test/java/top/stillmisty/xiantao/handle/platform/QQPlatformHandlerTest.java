package top.stillmisty.xiantao.handle.platform;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
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
import top.stillmisty.xiantao.service.NotificationAppender;

class QQPlatformHandlerTest {

  @Test
  void passesChoiceKeyboardWhenEnabled() {
    QqKeyboard keyboard = choiceKeyboard();
    NotificationAppender appender = appenderReturning(keyboard);
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();

    handler(appender, sender, true).replyText(message, "正文");

    verify(sender).replyMarkdown(message, "正文", keyboard);
  }

  @Test
  void omitsChoiceKeyboardWhenDisabled() {
    NotificationAppender appender = appenderReturning(choiceKeyboard());
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();

    handler(appender, sender, false).replyText(message, "正文");

    verify(sender).replyMarkdown(eq(message), eq("正文"), isNull());
  }

  private static NotificationAppender appenderReturning(QqKeyboard keyboard) {
    NotificationAppender appender = mock(NotificationAppender.class);
    when(appender.prepareAppend(eq(PlatformType.QQ), eq("OPEN-1"), eq("正文"), any()))
        .thenReturn(new NotificationAppender.AppendResult("正文", List.of(), keyboard));
    return appender;
  }

  @SuppressWarnings("unchecked")
  private static QQPlatformHandler handler(
      NotificationAppender appender, QqMessageSender sender, boolean choiceButtons) {
    ObjectProvider<QqMessageSender> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(sender);
    return new QQPlatformHandler(appender, provider, properties(choiceButtons));
  }

  private static QqKeyboard choiceKeyboard() {
    return QqKeyboard.commandGrid(List.of(QqButton.command("choice-0", "进入洞穴", "选 A")));
  }

  private static QqProperties properties(boolean choiceButtons) {
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
        false);
  }

  private static QqIncomingMessage message() {
    return new QqIncomingMessage(
        "EVENT-1", "MSG-1", QqScene.GROUP_AT, "OPEN-1", "GROUP-1", "状态", null, Instant.EPOCH);
  }
}
