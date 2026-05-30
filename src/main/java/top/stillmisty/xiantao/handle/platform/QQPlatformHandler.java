package top.stillmisty.xiantao.handle.platform;

import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
import love.forte.simbot.event.MessageEvent;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.NotificationAppender;

/** QQ 平台处理器 */
@Component
public class QQPlatformHandler implements PlatformHandler {

  private final NotificationAppender notificationAppender;

  public QQPlatformHandler(NotificationAppender notificationAppender) {
    this.notificationAppender = notificationAppender;
  }

  @Override
  public PlatformType getPlatformType() {
    return PlatformType.QQ;
  }

  @Override
  public boolean supports(MessageEvent event) {
    return event instanceof QGGroupAtMessageCreateEvent;
  }

  @Override
  public String extractOpenId(MessageEvent event) {
    if (event instanceof QGGroupAtMessageCreateEvent qqEvent) {
      return qqEvent.getAuthorId().toString();
    }
    throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
  }

  @Override
  public void replyText(MessageEvent event, String text) {
    if (event instanceof QGGroupAtMessageCreateEvent qqEvent) {
      var result =
          notificationAppender.prepareAppend(
              PlatformType.QQ, qqEvent.getAuthorId().toString(), text);
      qqEvent.replyBlocking(QGMarkdown.create(result.text()));
      notificationAppender.markDelivered(result.eventIds());
    } else {
      throw new IllegalArgumentException("不支持的事件类型: " + event.getClass().getName());
    }
  }
}
