package top.stillmisty.xiantao.handle.platform;

import love.forte.simbot.event.MessageEvent;
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
   * @param event 消息事件
   * @return 是否支持
   */
  boolean supports(MessageEvent event);

  /**
   * 从事件中提取 openId
   *
   * @param event 消息事件
   * @return 用户的 openId
   */
  String extractOpenId(MessageEvent event);

  /**
   * 回复文本消息
   *
   * @param event 消息事件
   * @param text 回复文本
   */
  void replyText(MessageEvent event, String text);
}
