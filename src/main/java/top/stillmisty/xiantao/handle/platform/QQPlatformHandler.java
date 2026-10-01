package top.stillmisty.xiantao.handle.platform;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqButton;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.qqgateway.QqMessageSender;
import top.stillmisty.xiantao.config.QqProperties;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.NextActions;
import top.stillmisty.xiantao.service.NotificationAppender;
import top.stillmisty.xiantao.util.TextFormat;

/** QQ 平台处理器 */
@Component
@Slf4j
public class QQPlatformHandler implements PlatformHandler {

  private final NotificationAppender notificationAppender;
  private final ObjectProvider<QqMessageSender> senderProvider;
  private final boolean choiceButtonsEnabled;
  private final boolean suggestionButtonsEnabled;

  public QQPlatformHandler(
      NotificationAppender notificationAppender,
      ObjectProvider<QqMessageSender> senderProvider,
      QqProperties properties) {
    this.notificationAppender = notificationAppender;
    this.senderProvider = senderProvider;
    this.choiceButtonsEnabled = Boolean.TRUE.equals(properties.choiceButtons());
    this.suggestionButtonsEnabled = Boolean.TRUE.equals(properties.suggestionButtons());
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
    replyText(message, text, List.of());
  }

  @Override
  public void replyText(
      QqIncomingMessage message, String text, List<NextActions.Suggestion> suggestions) {
    QqMessageSender sender = senderProvider.getIfAvailable();
    if (sender == null) {
      log.warn("QQ 机器人未配置（xiantao.qq.* 缺失），回复被丢弃: {}", abbreviate(text));
      return;
    }
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, message.openId(), text, TextFormat.get());
    // 选择事件按钮优先：玩家需要先做出选择；无选择事件时才展示下一步建议
    QqKeyboard keyboard = choiceButtonsEnabled ? result.keyboard() : null;
    if (keyboard == null && suggestionButtonsEnabled) {
      keyboard = suggestionKeyboard(suggestions);
    }
    sender.replyMarkdown(message, result.text(), keyboard);
    notificationAppender.markDelivered(result.eventIds());
  }

  /** 由下一步建议构建键盘：点击按钮即发送对应指令文本。 */
  private static @Nullable QqKeyboard suggestionKeyboard(List<NextActions.Suggestion> suggestions) {
    if (suggestions.isEmpty()) {
      return null;
    }
    List<QqButton> buttons = new ArrayList<>();
    for (NextActions.Suggestion suggestion : suggestions) {
      if (suggestion.command().isBlank()) {
        continue;
      }
      buttons.add(
          QqButton.command(
              "next-" + buttons.size(), truncateLabel(suggestion.label()), suggestion.command()));
      if (buttons.size() >= QqKeyboard.MAX_BUTTONS) {
        break;
      }
    }
    return buttons.isEmpty() ? null : QqKeyboard.commandGrid(buttons);
  }

  private static String truncateLabel(String text) {
    int maxCodePoints = 20;
    if (text.codePointCount(0, text.length()) <= maxCodePoints) {
      return text;
    }
    return text.substring(0, text.offsetByCodePoints(0, maxCodePoints)) + "…";
  }

  private static String abbreviate(String text) {
    return text.length() <= 80 ? text : text.substring(0, 80) + "...";
  }
}
