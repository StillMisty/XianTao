package top.stillmisty.xiantao.handle.platform;

import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.event.MessageEvent;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.NotificationAppender;

/** OneBotV11 平台处理器 */
@Component
public class OneBotV11PlatformHandler implements PlatformHandler {

  private final NotificationAppender notificationAppender;

  public OneBotV11PlatformHandler(NotificationAppender notificationAppender) {
    this.notificationAppender = notificationAppender;
  }

  @Override
  public PlatformType getPlatformType() {
    return PlatformType.ONE_BOT_V11;
  }

  @Override
  public boolean supports(MessageEvent event) {
    return event instanceof OneBotMessageEvent;
  }

  @Override
  public String extractOpenId(MessageEvent event) {
    if (event instanceof OneBotMessageEvent oneBotEvent) {
      return oneBotEvent.getAuthorId().toString();
    }
    throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
  }

  @Override
  public void replyText(MessageEvent event, String text) {
    if (event instanceof OneBotMessageEvent oneBotEvent) {
      var result =
          notificationAppender.prepareAppend(
              PlatformType.ONE_BOT_V11,
              oneBotEvent.getAuthorId().toString(),
              text,
              TextFormat.plain());
      oneBotEvent.replyBlocking(result.text());
      notificationAppender.markDelivered(result.eventIds());
    } else {
      throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
    }
  }
}
