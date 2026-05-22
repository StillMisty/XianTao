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
    log.debug("[OneBot] 收到GM帮助请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, gmCommandHandler::handleGmHelp);
  }

  @Listener
  @ContentTrim
  @Filter("GM给灵石 {{nickname}} {{amount}}")
  public void giveSpiritStones(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "[OneBot] 收到GM给灵石请求 - AuthorId: {}, Target: {}, Amount: {}",
        event.getAuthorId(),
        nickname,
        amount);
    replyHelper.oneBot(
        event, (p, o, f) -> gmCommandHandler.handleGiveSpiritStones(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给修为 {{nickname}} {{amount}}")
  public void giveExp(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "[OneBot] 收到GM给修为请求 - AuthorId: {}, Target: {}, Amount: {}",
        event.getAuthorId(),
        nickname,
        amount);
    replyHelper.oneBot(
        event, (p, o, f) -> gmCommandHandler.handleGiveExp(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM治疗 {{nickname}}")
  public void healUser(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.debug("[OneBot] 收到GM治疗请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    replyHelper.oneBot(event, nickname, gmCommandHandler::handleHealUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM复活 {{nickname}}")
  public void reviveUser(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.debug("[OneBot] 收到GM复活请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    replyHelper.oneBot(event, nickname, gmCommandHandler::handleReviveUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM等级 {{nickname}} {{level}}")
  public void setLevel(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    log.debug(
        "[OneBot] 收到GM等级请求 - AuthorId: {}, Target: {}, Level: {}",
        event.getAuthorId(),
        nickname,
        level);
    replyHelper.oneBot(
        event, (p, o, f) -> gmCommandHandler.handleSetLevel(p, o, nickname, level, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM传送 {{nickname}} {{locationName}}")
  public void setLocation(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("locationName") String locationName) {
    log.debug(
        "[OneBot] 收到GM传送请求 - AuthorId: {}, Target: {}, Location: {}",
        event.getAuthorId(),
        nickname,
        locationName);
    replyHelper.oneBot(
        event, (p, o, f) -> gmCommandHandler.handleSetLocation(p, o, nickname, locationName, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给物品 {{nickname}} {{itemName}} {{quantity}}")
  public void giveItem(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("itemName") String itemName,
      @FilterValue("quantity") String quantity) {
    log.debug(
        "[OneBot] 收到GM给物品请求 - AuthorId: {}, Target: {}, Item: {}, Quantity: {}",
        event.getAuthorId(),
        nickname,
        itemName,
        quantity);
    replyHelper.oneBot(
        event, (p, o, f) -> gmCommandHandler.handleGiveItem(p, o, nickname, itemName, quantity, f));
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("GM帮助")
  public void gmHelpQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到GM帮助请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, gmCommandHandler::handleGmHelp);
  }

  @Listener
  @ContentTrim
  @Filter("GM给灵石 {{nickname}} {{amount}}")
  public void giveSpiritStonesQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "[QQ] 收到GM给灵石请求 - AuthorId: {}, Target: {}, Amount: {}",
        event.getAuthorId(),
        nickname,
        amount);
    replyHelper.qq(
        event, (p, o, f) -> gmCommandHandler.handleGiveSpiritStones(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给修为 {{nickname}} {{amount}}")
  public void giveExpQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    log.debug(
        "[QQ] 收到GM给修为请求 - AuthorId: {}, Target: {}, Amount: {}",
        event.getAuthorId(),
        nickname,
        amount);
    replyHelper.qq(event, (p, o, f) -> gmCommandHandler.handleGiveExp(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM治疗 {{nickname}}")
  public void healUserQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("[QQ] 收到GM治疗请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    replyHelper.qq(event, nickname, gmCommandHandler::handleHealUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM复活 {{nickname}}")
  public void reviveUserQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.debug("[QQ] 收到GM复活请求 - AuthorId: {}, Target: {}", event.getAuthorId(), nickname);
    replyHelper.qq(event, nickname, gmCommandHandler::handleReviveUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM等级 {{nickname}} {{level}}")
  public void setLevelQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    log.debug(
        "[QQ] 收到GM等级请求 - AuthorId: {}, Target: {}, Level: {}",
        event.getAuthorId(),
        nickname,
        level);
    replyHelper.qq(event, (p, o, f) -> gmCommandHandler.handleSetLevel(p, o, nickname, level, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM传送 {{nickname}} {{locationName}}")
  public void setLocationQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("locationName") String locationName) {
    log.debug(
        "[QQ] 收到GM传送请求 - AuthorId: {}, Target: {}, Location: {}",
        event.getAuthorId(),
        nickname,
        locationName);
    replyHelper.qq(
        event, (p, o, f) -> gmCommandHandler.handleSetLocation(p, o, nickname, locationName, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给物品 {{nickname}} {{itemName}} {{quantity}}")
  public void giveItemQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("itemName") String itemName,
      @FilterValue("quantity") String quantity) {
    log.debug(
        "[QQ] 收到GM给物品请求 - AuthorId: {}, Target: {}, Item: {}, Quantity: {}",
        event.getAuthorId(),
        nickname,
        itemName,
        quantity);
    replyHelper.qq(
        event, (p, o, f) -> gmCommandHandler.handleGiveItem(p, o, nickname, itemName, quantity, f));
  }
}
