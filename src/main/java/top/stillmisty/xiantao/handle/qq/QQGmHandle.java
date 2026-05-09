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
import top.stillmisty.xiantao.handle.command.GmCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class QQGmHandle {

  private final GmCommandHandler gmCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("GM帮助")
  public void gmHelp(QGGroupAtMessageCreateEvent event) {
    log.debug("收到GM帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        gmCommandHandler.handleGmHelpMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给灵石 {{nickname}} {{amount}}")
  public void giveSpiritStones(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "收到GM给灵石请求 - AuthorId: {}, Target: {}, Amount: {}", event.getAuthorId(), nickname, amount);
    String response =
        gmCommandHandler.handleGiveSpiritStonesMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, amount);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给修为 {{nickname}} {{amount}}")
  public void giveExp(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "收到GM给修为请求 - AuthorId: {}, Target: {}, Amount: {}", event.getAuthorId(), nickname, amount);
    String response =
        gmCommandHandler.handleGiveExpMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, amount);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM治疗 {{nickname}}")
  public void healUser(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到GM治疗请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    String response =
        gmCommandHandler.handleHealUserMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM复活 {{nickname}}")
  public void reviveUser(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到GM复活请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    String response =
        gmCommandHandler.handleReviveUserMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM设置等级 {{nickname}} {{level}}")
  public void setLevel(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    log.debug(
        "收到GM设置等级请求 - AuthorId: {}, Target: {}, Level: {}", event.getAuthorId(), nickname, level);
    String response =
        gmCommandHandler.handleSetLevelMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, level);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM设置所在地点 {{nickname}} {{locationName}}")
  public void setLocation(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("locationName") String locationName) {
    log.debug(
        "收到GM设置所在地点请求 - AuthorId: {}, Target: {}, Location: {}",
        event.getAuthorId(),
        nickname,
        locationName);
    String response =
        gmCommandHandler.handleSetLocationMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, locationName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给物品和装备 {{nickname}} {{itemName}} {{quantity}}")
  public void giveItem(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("itemName") String itemName,
      @FilterValue("quantity") String quantity) {
    log.debug(
        "收到GM给物品和装备请求 - AuthorId: {}, Target: {}, Item: {}, Quantity: {}",
        event.getAuthorId(),
        nickname,
        itemName,
        quantity);
    String response =
        gmCommandHandler.handleGiveItemMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, itemName, quantity);
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
