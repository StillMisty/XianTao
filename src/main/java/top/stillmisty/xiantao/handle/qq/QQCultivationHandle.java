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
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class QQCultivationHandle {

  private final CultivationCommandHandler cultivationCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("改号 {{newNickname}}")
  public void changeNickname(
      QGGroupAtMessageCreateEvent event, @FilterValue("newNickname") String newNickname) {
    log.debug("收到改号请求 - AuthorId: {}, NewNickname: {}", event.getAuthorId(), newNickname);
    String response =
        cultivationCommandHandler.handleChangeNicknameMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), newNickname);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("突破")
  public void breakthrough(QGGroupAtMessageCreateEvent event) {
    log.debug("收到突破请求 - AuthorId: {}", event.getAuthorId());
    String response =
        cultivationCommandHandler.handleBreakthroughMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("护道 {{nickname}}")
  public void establishProtection(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到护道请求 - AuthorId: {}, Content: {}", event.getAuthorId(), nickname);
    String response =
        cultivationCommandHandler.handleEstablishProtectionMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("护道解除 {{nickname}}")
  public void removeProtection(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到护道解除请求 - AuthorId: {}, Content: {}", event.getAuthorId(), nickname);
    String response =
        cultivationCommandHandler.handleRemoveProtectionMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("护道查询")
  public void queryProtection(QGGroupAtMessageCreateEvent event) {
    log.debug("收到护道查询请求 - AuthorId: {}", event.getAuthorId());
    String response =
        cultivationCommandHandler.handleQueryProtectionMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
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
