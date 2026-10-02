package top.stillmisty.xiantao.handle.platform;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqButton;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.xiantao.handle.NextActions;
import top.stillmisty.xiantao.service.NotificationAppender;
import top.stillmisty.xiantao.util.TextFormat;

/**
 * 回复投递 — 一条回复的完整投递顺序：追加未投递事件 → 选择按钮优先 → 按平台限额分段 → 发送 → 全部成功后才标记投递。
 *
 * <p>平台适配器只负责 transport（{@link PlatformHandler#sendReply}）与能力声明（{@link
 * PlatformHandler#replyLimits()}）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyDelivery {

  private final NotificationAppender notificationAppender;

  /** 投递一条回复。发送失败时抛出，由调用方决定兜底。 */
  public void deliver(
      PlatformHandler handler,
      QqIncomingMessage message,
      String text,
      List<NextActions.Suggestion> suggestions) {
    NotificationAppender.AppendResult prepared =
        notificationAppender.prepareAppend(
            handler.getPlatformType(), handler.extractOpenId(message), text, TextFormat.get());

    ReplyLimits limits = handler.replyLimits();
    // 选择事件按钮优先：玩家需要先做出选择；无选择事件时才展示下一步建议
    QqKeyboard keyboard = limits.choiceButtons() ? prepared.keyboard() : null;
    if (keyboard == null && limits.suggestionButtons()) {
      keyboard = suggestionKeyboard(suggestions, limits.maxButtonLabelChars());
    }

    List<String> segments =
        MessageSegments.split(prepared.text(), limits.maxSegmentBytes(), limits.maxSegments());
    handler.sendReply(message, segments, keyboard);
    notificationAppender.markDelivered(prepared.eventIds());
  }

  /** 由下一步建议构建键盘：点击按钮即发送对应指令文本。 */
  private static @Nullable QqKeyboard suggestionKeyboard(
      List<NextActions.Suggestion> suggestions, int maxLabelChars) {
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
              "next-" + buttons.size(),
              truncateLabel(suggestion.label(), maxLabelChars),
              suggestion.command()));
      if (buttons.size() >= QqKeyboard.MAX_BUTTONS) {
        break;
      }
    }
    return buttons.isEmpty() ? null : QqKeyboard.commandGrid(buttons);
  }

  private static String truncateLabel(String text, int maxCodePoints) {
    if (text.codePointCount(0, text.length()) <= maxCodePoints) {
      return text;
    }
    return text.substring(0, text.offsetByCodePoints(0, maxCodePoints - 1)) + "…";
  }
}
