package top.stillmisty.xiantao.handle.platform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;
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

/** QQ 适配器只负责 transport 与能力声明；投递顺序由 ReplyDeliveryTest 覆盖。 */
class QQPlatformHandlerTest {

  @Test
  void declaresPlatformLimitsAndButtonSwitches() {
    ReplyLimits limits = handler(mock(QqMessageSender.class), true, false).replyLimits();

    assertEquals(QQPlatformHandler.MAX_SEGMENT_BYTES, limits.maxSegmentBytes());
    assertEquals(QQPlatformHandler.MAX_SEGMENTS, limits.maxSegments());
    assertEquals(QQPlatformHandler.MAX_BUTTON_LABEL_CHARS, limits.maxButtonLabelChars());
    assertEquals(true, limits.choiceButtons());
    assertEquals(false, limits.suggestionButtons());
  }

  @Test
  void sendsKeyboardOnlyOnLastSegment() {
    QqMessageSender sender = mock(QqMessageSender.class);
    QqIncomingMessage message = message();
    QqKeyboard keyboard =
        QqKeyboard.commandGrid(List.of(QqButton.command("choice-0", "进入", "选 A")));

    handler(sender, true, true).sendReply(message, List.of("第一段", "第二段"), keyboard);

    verify(sender).replyMarkdown(message, "第一段", null);
    verify(sender).replyMarkdown(message, "第二段", keyboard);
  }

  @Test
  void dropsReplyWithoutConfiguredSender() {
    ObjectProvider<QqMessageSender> provider = provider(null);
    QQPlatformHandler handler = new QQPlatformHandler(provider, properties(true, true));

    assertDoesNotThrow(() -> handler.sendReply(message(), List.of("正文"), null));
  }

  @SuppressWarnings("unchecked")
  private static QQPlatformHandler handler(
      QqMessageSender sender, boolean choiceButtons, boolean suggestionButtons) {
    return new QQPlatformHandler(provider(sender), properties(choiceButtons, suggestionButtons));
  }

  @SuppressWarnings("unchecked")
  private static ObjectProvider<QqMessageSender> provider(@Nullable QqMessageSender sender) {
    ObjectProvider<QqMessageSender> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(sender);
    return provider;
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
