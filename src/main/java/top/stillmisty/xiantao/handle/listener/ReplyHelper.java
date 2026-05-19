package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.NotificationAppender;

/** 平台回复辅助 集中处理 OneBotV11 / QQ 两种回复方式 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReplyHelper {

  private final NotificationAppender notificationAppender;

  public void replyOneBot(OneBotMessageEvent event, String text) {
    try {
      event.replyBlocking(
          notificationAppender
              .prepareAppend(PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), text)
              .text());
    } catch (Exception e) {
      log.warn("OneBot 回复失败: {}", e.getMessage());
    }
  }

  public void replyQQ(QGGroupAtMessageCreateEvent event, String text) {
    try {
      var result =
          notificationAppender.prepareAppend(PlatformType.QQ, event.getAuthorId().toString(), text);
      event.replyBlocking(QGMarkdown.create(result.text()));
      notificationAppender.markDelivered(result.eventIds());
    } catch (Exception e) {
      log.warn("QQ 回复失败: {}", e.getMessage());
    }
  }
}
