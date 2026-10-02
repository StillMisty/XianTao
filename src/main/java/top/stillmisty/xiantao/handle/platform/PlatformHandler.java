package top.stillmisty.xiantao.handle.platform;

import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.qqgateway.QqKeyboard;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

/** 平台处理器接口 每个平台实现此接口，提供平台特定的处理逻辑 */
public interface PlatformHandler {

  /**
   * 获取平台类型
   *
   * @return 平台类型枚举
   */
  PlatformType getPlatformType();

  /**
   * 检查是否支持指定的事件类型
   *
   * @param message 消息事件
   * @return 是否支持
   */
  boolean supports(QqIncomingMessage message);

  /**
   * 从事件中提取 openId
   *
   * @param message 消息事件
   * @return 用户的 openId
   */
  String extractOpenId(QqIncomingMessage message);

  /**
   * 平台回复能力：分段上限、按钮开关与按钮文字上限
   *
   * @return 平台能力声明
   */
  ReplyLimits replyLimits();

  /**
   * 发送一条已装配好的回复（transport）。投递顺序、通知追加与标记由 {@link ReplyDelivery} 负责。
   *
   * @param message 消息事件
   * @param segments 已分段的回复文本（至少一段）
   * @param keyboard 末段附带的可选按钮键盘
   */
  void sendReply(QqIncomingMessage message, List<String> segments, @Nullable QqKeyboard keyboard);
}
