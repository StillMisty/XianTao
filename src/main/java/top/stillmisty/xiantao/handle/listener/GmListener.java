package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.GmCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.handle.interceptor.RequireGm;

@Component
@RequiredArgsConstructor
public class GmListener {

  private final GmCommandHandler gmCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM帮助")
  public void gmHelp(MessageEvent event) {
    replyHelper.dispatch(event, "GM帮助", gmCommandHandler::handleGmHelp);
  }

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM给灵石\\s*{{nickname}}\\s+{{amount}}")
  public void giveSpiritStones(
      MessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    replyHelper.dispatch(
        event, "GM给灵石", fmt -> gmCommandHandler.handleGiveSpiritStones(nickname, amount, fmt));
  }

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM给修为\\s*{{nickname}}\\s+{{amount}}")
  public void giveExp(
      MessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("amount") String amount) {
    replyHelper.dispatch(
        event, "GM给修为", fmt -> gmCommandHandler.handleGiveExp(nickname, amount, fmt));
  }

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM治疗\\s*{{nickname}}")
  public void healUser(MessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.dispatch(event, "GM治疗", nickname, gmCommandHandler::handleHealUser);
  }

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM复活\\s*{{nickname}}")
  public void reviveUser(MessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.dispatch(event, "GM复活", nickname, gmCommandHandler::handleReviveUser);
  }

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM等级\\s*{{nickname}}\\s+{{level}}")
  public void setLevel(
      MessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("level") String level) {
    replyHelper.dispatch(
        event, "GM等级", fmt -> gmCommandHandler.handleSetLevel(nickname, level, fmt));
  }

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM传送\\s*{{nickname}}\\s+{{locationName}}")
  public void setLocation(
      MessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("locationName") String locationName) {
    replyHelper.dispatch(
        event, "GM传送", fmt -> gmCommandHandler.handleSetLocation(nickname, locationName, fmt));
  }

  @RequireAuth
  @RequireGm
  @Listener
  @ContentTrim
  @Filter("GM给物品\\s*{{nickname}}\\s+{{itemName}}\\s+{{quantity}}")
  public void giveItem(
      MessageEvent event,
      @FilterValue("nickname") String nickname,
      @FilterValue("itemName") String itemName,
      @FilterValue("quantity") String quantity) {
    replyHelper.dispatch(
        event, "GM给物品", fmt -> gmCommandHandler.handleGiveItem(nickname, itemName, quantity, fmt));
  }
}
