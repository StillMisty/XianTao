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

  /** 单条 markdown 内容的 UTF-8 字节上限（社区实测平台约 2000 字节，留出安全余量）。 */
  static final int MAX_SEGMENT_BYTES = 1800;

  /** 单条用户消息最多回复段数（平台被动回复上限 5 次/条，为处理中提示留出余量）。 */
  static final int MAX_SEGMENTS = 4;

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
    List<String> segments = splitMessage(result.text());
    for (int index = 0; index < segments.size(); index++) {
      boolean lastSegment = index == segments.size() - 1;
      sender.replyMarkdown(message, segments.get(index), lastSegment ? keyboard : null);
    }
    notificationAppender.markDelivered(result.eventIds());
  }

  /**
   * 按 QQ markdown 内容上限分段：UTF-8 约 1800 字节/段，最多 4 段（被动回复上限 5 次/条），超出截断并标注。
   *
   * <p>优先在换行处切分，避免把一行内容劈成两半。
   */
  static List<String> splitMessage(String text) {
    List<String> parts = new ArrayList<>();
    int start = 0;
    while (start < text.length()) {
      int end = byteLimitedEnd(text, start, MAX_SEGMENT_BYTES);
      if (end >= text.length()) {
        parts.add(text.substring(start));
        break;
      }
      if (parts.size() == MAX_SEGMENTS - 1) {
        int keep = byteLimitedEnd(text, start, MAX_SEGMENT_BYTES - 64);
        parts.add(text.substring(start, keep).stripTrailing() + "\n……（内容过长，后续已省略）");
        break;
      }
      int cut = preferLineBreak(text, start, end);
      parts.add(text.substring(start, cut).strip());
      start = cut;
    }
    return parts.isEmpty() ? List.of(text) : parts;
  }

  /** [start, end) 中不超过 maxBytes 的最大结束位置（不切开代理对）。 */
  private static int byteLimitedEnd(String text, int start, int maxBytes) {
    int bytes = 0;
    int index = start;
    while (index < text.length()) {
      int codePoint = text.codePointAt(index);
      int byteCount = utf8Length(codePoint);
      if (bytes + byteCount > maxBytes) {
        break;
      }
      bytes += byteCount;
      index += Character.charCount(codePoint);
    }
    return index;
  }

  private static int utf8Length(int codePoint) {
    if (codePoint < 0x80) {
      return 1;
    }
    if (codePoint < 0x800) {
      return 2;
    }
    if (codePoint < 0x10000) {
      return 3;
    }
    return 4;
  }

  /** 尽量在靠后的换行处切分（至少保留一半内容，避免切得太碎）。 */
  private static int preferLineBreak(String text, int start, int end) {
    int newline = text.lastIndexOf('\n', end - 1);
    if (newline > start + (end - start) / 2) {
      return newline + 1;
    }
    return end;
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
