package top.stillmisty.xiantao.handle.platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.qqgateway.QqMessageSender;
import top.stillmisty.xiantao.config.QqProperties;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.NotificationAppender;
import top.stillmisty.xiantao.util.TextFormat;

/** QQ 平台处理器 */
@Component
@Slf4j
public class QQPlatformHandler implements PlatformHandler {

  private final NotificationAppender notificationAppender;
  private final ObjectProvider<QqMessageSender> senderProvider;
  private final boolean choiceButtonsEnabled;

  public QQPlatformHandler(
      NotificationAppender notificationAppender,
      ObjectProvider<QqMessageSender> senderProvider,
      QqProperties properties) {
    this.notificationAppender = notificationAppender;
    this.senderProvider = senderProvider;
    this.choiceButtonsEnabled = Boolean.TRUE.equals(properties.choiceButtons());
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
  public void replyText(QqIncomingMessage message, String text) {
    QqMessageSender sender = senderProvider.getIfAvailable();
    if (sender == null) {
      log.warn("QQ 机器人未配置（xiantao.qq.* 缺失），回复被丢弃: {}", abbreviate(text));
      return;
    }
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, message.openId(), text, TextFormat.get());
    QqKeyboard keyboard = choiceButtonsEnabled ? result.keyboard() : null;
    sender.replyMarkdown(message, result.text(), keyboard);
    notificationAppender.markDelivered(result.eventIds());
  }

  private static String abbreviate(String text) {
    return text.length() <= 80 ? text : text.substring(0, 80) + "...";
  }
}
