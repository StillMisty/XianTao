package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.GmCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.handle.interceptor.RequireGm;

@Component
@RequiredArgsConstructor
@CommandGroup
public class GmListener {

  private final GmCommandHandler gmCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @RequireGm
  @Command("GM帮助")
  public void gmHelp(QqIncomingMessage event) {
    replyHelper.dispatch(event, "GM帮助", gmCommandHandler::handleGmHelp);
  }

  @RequireAuth
  @RequireGm
  @Command("GM给灵石\\s*{{nickname}}\\s+{{amount}}")
  public void giveSpiritStones(
      QqIncomingMessage event, @Arg("nickname") String nickname, @Arg("amount") String amount) {
    replyHelper.dispatch(
        event, "GM给灵石", fmt -> gmCommandHandler.handleGiveSpiritStones(nickname, amount, fmt));
  }

  @RequireAuth
  @RequireGm
  @Command("GM给修为\\s*{{nickname}}\\s+{{amount}}")
  public void giveExp(
      QqIncomingMessage event, @Arg("nickname") String nickname, @Arg("amount") String amount) {
    replyHelper.dispatch(
        event, "GM给修为", fmt -> gmCommandHandler.handleGiveExp(nickname, amount, fmt));
  }

  @RequireAuth
  @RequireGm
  @Command("GM治疗\\s*{{nickname}}")
  public void healUser(QqIncomingMessage event, @Arg("nickname") String nickname) {
    replyHelper.dispatch(event, "GM治疗", nickname, gmCommandHandler::handleHealUser);
  }

  @RequireAuth
  @RequireGm
  @Command("GM复活\\s*{{nickname}}")
  public void reviveUser(QqIncomingMessage event, @Arg("nickname") String nickname) {
    replyHelper.dispatch(event, "GM复活", nickname, gmCommandHandler::handleReviveUser);
  }

  @RequireAuth
  @RequireGm
  @Command("GM等级\\s*{{nickname}}\\s+{{level}}")
  public void setLevel(
      QqIncomingMessage event, @Arg("nickname") String nickname, @Arg("level") String level) {
    replyHelper.dispatch(
        event, "GM等级", fmt -> gmCommandHandler.handleSetLevel(nickname, level, fmt));
  }

  @RequireAuth
  @RequireGm
  @Command("GM传送\\s*{{nickname}}\\s+{{locationName}}")
  public void setLocation(
      QqIncomingMessage event,
      @Arg("nickname") String nickname,
      @Arg("locationName") String locationName) {
    replyHelper.dispatch(
        event, "GM传送", fmt -> gmCommandHandler.handleSetLocation(nickname, locationName, fmt));
  }

  @RequireAuth
  @RequireGm
  @Command("GM给物品\\s*{{nickname,\\S+}}\\s+{{itemName,\\S+}}\\s+{{quantity,\\d+}}")
  public void giveItem(
      QqIncomingMessage event,
      @Arg("nickname") String nickname,
      @Arg("itemName") String itemName,
      @Arg("quantity") String quantity) {
    replyHelper.dispatch(
        event, "GM给物品", fmt -> gmCommandHandler.handleGiveItem(nickname, itemName, quantity, fmt));
  }
}
