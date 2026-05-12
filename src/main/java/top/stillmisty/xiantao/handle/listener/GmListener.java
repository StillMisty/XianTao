package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.GmCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmListener {

  private final GmCommandHandler gmCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("GM帮助")
  public void gmHelp(OneBotMessageEvent event) {
    log.debug("收到GM帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        gmCommandHandler.handleGmHelp(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给灵石 {{nickname}} {{amount}}")
  public void giveSpiritStones(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "收到GM给灵石请求 - AuthorId: {}, Target: {}, Amount: {}", event.getAuthorId(), nickname, amount);
    String response =
        gmCommandHandler.handleGiveSpiritStones(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname, amount);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给修为 {{nickname}} {{amount}}")
  public void giveExp(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "收到GM给修为请求 - AuthorId: {}, Target: {}, Amount: {}", event.getAuthorId(), nickname, amount);
    String response =
        gmCommandHandler.handleGiveExp(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname, amount);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM治疗 {{nickname}}")
  public void healUser(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到GM治疗请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    String response =
        gmCommandHandler.handleHealUser(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM复活 {{nickname}}")
  public void reviveUser(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到GM复活请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    String response =
        gmCommandHandler.handleReviveUser(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM设置等级 {{nickname}} {{level}}")
  public void setLevel(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    log.debug(
        "收到GM设置等级请求 - AuthorId: {}, Target: {}, Level: {}", event.getAuthorId(), nickname, level);
    String response =
        gmCommandHandler.handleSetLevel(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname, level);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM设置所在地点 {{nickname}} {{locationName}}")
  public void setLocation(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("locationName") String locationName) {
    log.debug(
        "收到GM设置所在地点请求 - AuthorId: {}, Target: {}, Location: {}",
        event.getAuthorId(),
        nickname,
        locationName);
    String response =
        gmCommandHandler.handleSetLocation(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname, locationName);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给物品和装备 {{nickname}} {{itemName}} {{quantity}}")
  public void giveItem(
      OneBotMessageEvent event,
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
        gmCommandHandler.handleGiveItem(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname, itemName, quantity);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("GM帮助")
  public void gmHelpQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到GM帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        gmCommandHandler.handleGmHelpMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给灵石 {{nickname}} {{amount}}")
  public void giveSpiritStonesQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "收到GM给灵石请求 - AuthorId: {}, Target: {}, Amount: {}", event.getAuthorId(), nickname, amount);
    String response =
        gmCommandHandler.handleGiveSpiritStonesMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, amount);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给修为 {{nickname}} {{amount}}")
  public void giveExpQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "收到GM给修为请求 - AuthorId: {}, Target: {}, Amount: {}", event.getAuthorId(), nickname, amount);
    String response =
        gmCommandHandler.handleGiveExpMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, amount);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM治疗 {{nickname}}")
  public void healUserQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到GM治疗请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    String response =
        gmCommandHandler.handleHealUserMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM复活 {{nickname}}")
  public void reviveUserQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("收到GM复活请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    String response =
        gmCommandHandler.handleReviveUserMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM设置等级 {{nickname}} {{level}}")
  public void setLevelQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    log.debug(
        "收到GM设置等级请求 - AuthorId: {}, Target: {}, Level: {}", event.getAuthorId(), nickname, level);
    String response =
        gmCommandHandler.handleSetLevelMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname, level);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM设置所在地点 {{nickname}} {{locationName}}")
  public void setLocationQq(
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
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("GM给物品和装备 {{nickname}} {{itemName}} {{quantity}}")
  public void giveItemQq(
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
    replyHelper.replyQQ(event, response);
  }
}
