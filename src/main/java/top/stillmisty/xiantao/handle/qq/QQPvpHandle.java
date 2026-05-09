package top.stillmisty.xiantao.handle.qq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.PvpCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class QQPvpHandle {

  private final PvpCommandHandler pvpCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("切磋 {{targetNickname}}")
  public void spar(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    log.debug("收到切磋请求 - AuthorId: {}, Target: {}", event.getAuthorId(), targetNickname);
    String response =
        pvpCommandHandler.handleSparMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), targetNickname);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(QGGroupAtMessageCreateEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, event.getAuthorId().toString(), response);
    event.replyBlocking(QGMarkdown.create(result.text()));
    notificationAppender.markDelivered(result.eventIds());
  }
}
