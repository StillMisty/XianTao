package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
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
public class PvpHandle {

  private final PvpCommandHandler pvpCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("切磋 {{targetNickname}}")
  public void spar(MessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    log.debug("收到切磋请求 - AuthorId: {}, Target: {}", event.getAuthorId(), targetNickname);
    String response =
        pvpCommandHandler.handleSpar(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), targetNickname);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(MessageEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), response);
    event.replyBlocking(result.text());
    notificationAppender.markDelivered(result.eventIds());
  }
}
