package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.GmCommandHandler;

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
    replyHelper.oneBot(event, "GM帮助", gmCommandHandler::handleGmHelp);
  }

  @Listener
  @ContentTrim
  @Filter("GM给灵石\\s*{{nickname}}\\s+{{amount}}")
  public void giveSpiritStones(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    replyHelper.oneBot(
        event,
        "GM给灵石",
        (p, o, f) -> gmCommandHandler.handleGiveSpiritStones(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给修为\\s*{{nickname}}\\s+{{amount}}")
  public void giveExp(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    replyHelper.oneBot(
        event, "GM给修为", (p, o, f) -> gmCommandHandler.handleGiveExp(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM治疗\\s*{{nickname}}")
  public void healUser(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.oneBot(event, "GM治疗", nickname, gmCommandHandler::handleHealUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM复活\\s*{{nickname}}")
  public void reviveUser(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.oneBot(event, "GM复活", nickname, gmCommandHandler::handleReviveUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM等级\\s*{{nickname}}\\s+{{level}}")
  public void setLevel(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    replyHelper.oneBot(
        event, "GM等级", (p, o, f) -> gmCommandHandler.handleSetLevel(p, o, nickname, level, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM传送\\s*{{nickname}}\\s+{{locationName}}")
  public void setLocation(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("locationName") String locationName) {
    replyHelper.oneBot(
        event,
        "GM传送",
        (p, o, f) -> gmCommandHandler.handleSetLocation(p, o, nickname, locationName, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给物品\\s*{{nickname}}\\s+{{itemName}}\\s+{{quantity}}")
  public void giveItem(
      OneBotMessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("itemName") String itemName,
      @FilterValue("quantity") String quantity) {
    replyHelper.oneBot(
        event,
        "GM给物品",
        (p, o, f) -> gmCommandHandler.handleGiveItem(p, o, nickname, itemName, quantity, f));
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("GM帮助")
  public void gmHelpQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "GM帮助", gmCommandHandler::handleGmHelp);
  }

  @Listener
  @ContentTrim
  @Filter("GM给灵石\\s*{{nickname}}\\s+{{amount}}")
  public void giveSpiritStonesQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    replyHelper.qq(
        event,
        "GM给灵石",
        (p, o, f) -> gmCommandHandler.handleGiveSpiritStones(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给修为\\s*{{nickname}}\\s+{{amount}}")
  public void giveExpQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    replyHelper.qq(
        event, "GM给修为", (p, o, f) -> gmCommandHandler.handleGiveExp(p, o, nickname, amount, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM治疗\\s*{{nickname}}")
  public void healUserQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.qq(event, "GM治疗", nickname, gmCommandHandler::handleHealUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM复活\\s*{{nickname}}")
  public void reviveUserQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.qq(event, "GM复活", nickname, gmCommandHandler::handleReviveUser);
  }

  @Listener
  @ContentTrim
  @Filter("GM等级\\s*{{nickname}}\\s+{{level}}")
  public void setLevelQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    replyHelper.qq(
        event, "GM等级", (p, o, f) -> gmCommandHandler.handleSetLevel(p, o, nickname, level, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM传送\\s*{{nickname}}\\s+{{locationName}}")
  public void setLocationQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("locationName") String locationName) {
    replyHelper.qq(
        event,
        "GM传送",
        (p, o, f) -> gmCommandHandler.handleSetLocation(p, o, nickname, locationName, f));
  }

  @Listener
  @ContentTrim
  @Filter("GM给物品\\s*{{nickname}}\\s+{{itemName}}\\s+{{quantity}}")
  public void giveItemQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("itemName") String itemName,
      @FilterValue("quantity") String quantity) {
    replyHelper.qq(
        event,
        "GM给物品",
        (p, o, f) -> gmCommandHandler.handleGiveItem(p, o, nickname, itemName, quantity, f));
  }
}
