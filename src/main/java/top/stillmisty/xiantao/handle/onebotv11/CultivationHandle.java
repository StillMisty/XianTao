package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class CultivationHandle {

  private final CultivationCommandHandler cultivationCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("突破")
  public void breakthrough(OneBotMessageEvent event) {
    log.debug("收到突破请求 - AuthorId: {}", event.getAuthorId());
    String response =
        cultivationCommandHandler.handleBreakthrough(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("护道 {{nickname}}")
  public void establishProtection(
      OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到护道请求 - AuthorId: {}, Content: {}", event.getAuthorId(), nickname);
    String response =
        cultivationCommandHandler.handleEstablishProtection(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("护道解除 {{nickname}}")
  public void removeProtection(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到护道解除请求 - AuthorId: {}, Content: {}", event.getAuthorId(), nickname);
    String response =
        cultivationCommandHandler.handleRemoveProtection(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("护道查询")
  public void queryProtection(OneBotMessageEvent event) {
    log.debug("收到护道查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        cultivationCommandHandler.handleQueryProtection(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(OneBotMessageEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), response);
    event.replyBlocking(result.text());
    notificationAppender.markDelivered(result.eventIds());
  }
}
