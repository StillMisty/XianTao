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
import top.stillmisty.xiantao.handle.command.MapCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class QQMapHandle {

  private final MapCommandHandler mapCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("地图")
  public void currentMap(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleCurrentMapMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地图列表")
  public void mapList(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleMapListMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("前往 {{mapName}}")
  public void goTo(QGGroupAtMessageCreateEvent event, @FilterValue("mapName") String mapName) {
    String response =
        mapCommandHandler.handleGoToMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), mapName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练")
  public void training(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleTrainingMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练结算")
  public void endTraining(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleEndTrainingMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏列表")
  public void bountyList(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleBountyListMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏")
  public void bountyStatus(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleBountyStatusMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏接取 {{bountyId}}")
  public void startBounty(
      QGGroupAtMessageCreateEvent event, @FilterValue("bountyId") String bountyId) {
    String response =
        mapCommandHandler.handleStartBountyMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), bountyId);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏结算")
  public void completeBounty(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleCompleteBountyMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏放弃")
  public void abandonBounty(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleAbandonBountyMarkdown(
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
