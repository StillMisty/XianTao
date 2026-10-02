package top.stillmisty.xiantao.handle.platform;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.qqgateway.QqMessageSender;
import top.stillmisty.xiantao.config.QqProperties;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

/** QQ 平台适配器 — 只负责 transport 与平台能力声明，投递顺序由 {@link ReplyDelivery} 负责。 */
@Component
@Slf4j
public class QQPlatformHandler implements PlatformHandler {

  /** 单条 markdown 内容的 UTF-8 字节上限（社区实测平台约 2000 字节，留出安全余量）。 */
  static final int MAX_SEGMENT_BYTES = 1800;

  /** 单条用户消息最多回复段数（平台被动回复上限 5 次/条，为处理中提示留出余量）。 */
  static final int MAX_SEGMENTS = 4;

  /** 按钮文字（render_data.label）最多 10 字符，留一位给省略号 */
  static final int MAX_BUTTON_LABEL_CHARS = 10;

  private final ObjectProvider<QqMessageSender> senderProvider;
  private final boolean choiceButtonsEnabled;
  private final boolean suggestionButtonsEnabled;

  public QQPlatformHandler(
      ObjectProvider<QqMessageSender> senderProvider, QqProperties properties) {
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
  public ReplyLimits replyLimits() {
    return new ReplyLimits(
        MAX_SEGMENT_BYTES,
        MAX_SEGMENTS,
        MAX_BUTTON_LABEL_CHARS,
        choiceButtonsEnabled,
        suggestionButtonsEnabled);
  }

  @Override
  public void sendReply(
      QqIncomingMessage message, List<String> segments, @Nullable QqKeyboard keyboard) {
    QqMessageSender sender = senderProvider.getIfAvailable();
    if (sender == null) {
      log.warn("QQ 机器人未配置（xiantao.qq.* 缺失），回复被丢弃: {}", abbreviate(segments));
      return;
    }
    for (int index = 0; index < segments.size(); index++) {
      boolean lastSegment = index == segments.size() - 1;
      sender.replyMarkdown(message, segments.get(index), lastSegment ? keyboard : null);
    }
  }

  private static String abbreviate(List<String> segments) {
    String text = segments.isEmpty() ? "" : segments.getFirst();
    return text.length() <= 80 ? text : text.substring(0, 80) + "...";
  }
}
