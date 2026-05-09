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
import top.stillmisty.xiantao.handle.command.MapCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapHandle {

  private final MapCommandHandler mapCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("地图")
  public void currentMap(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleCurrentMap(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地图列表")
  public void mapList(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleMapList(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("前往 {{mapName}}")
  public void goTo(OneBotMessageEvent event, @FilterValue("mapName") String mapName) {
    String response =
        mapCommandHandler.handleGoTo(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), mapName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练")
  public void training(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleTraining(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练结算")
  public void endTraining(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleEndTraining(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏列表")
  public void bountyList(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleBountyList(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏")
  public void bountyStatus(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleBountyStatus(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏接取 {{bountyId}}")
  public void startBounty(OneBotMessageEvent event, @FilterValue("bountyId") String bountyId) {
    String response =
        mapCommandHandler.handleStartBounty(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), bountyId);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏结算")
  public void completeBounty(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleCompleteBounty(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏放弃")
  public void abandonBounty(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleAbandonBounty(
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
