package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.MapCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class MapListener {
  private final MapCommandHandler mapCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("地图")
  public void currentMap(QqIncomingMessage event) {
    replyHelper.dispatch(event, "地图", mapCommandHandler::handleMap);
  }

  @RequireAuth
  @Command("前往\\s*{{mapName}}")
  public void goTo(QqIncomingMessage event, @Arg("mapName") String mapName) {
    replyHelper.dispatch(event, "前往", mapName, mapCommandHandler::handleGoTo);
  }

  @RequireAuth
  @Command("历练")
  public void training(QqIncomingMessage event) {
    replyHelper.dispatch(event, "历练", mapCommandHandler::handleTraining);
  }

  @RequireAuth
  @Command("历练结算")
  public void endTraining(QqIncomingMessage event) {
    replyHelper.dispatch(event, "历练结算", mapCommandHandler::handleEndTraining);
  }

  @RequireAuth
  @Command("悬赏")
  public void bounty(QqIncomingMessage event) {
    replyHelper.dispatch(event, "悬赏", mapCommandHandler::handleBounty);
  }

  @RequireAuth
  @Command("悬赏接取\\s*{{bountyId}}")
  public void startBounty(QqIncomingMessage event, @Arg("bountyId") String bountyId) {
    replyHelper.dispatch(event, "悬赏接取", bountyId, mapCommandHandler::handleStartBounty);
  }

  @RequireAuth
  @Command("悬赏结算")
  public void completeBounty(QqIncomingMessage event) {
    replyHelper.dispatch(event, "悬赏结算", mapCommandHandler::handleCompleteBounty);
  }

  @RequireAuth
  @Command("悬赏放弃")
  public void abandonBounty(QqIncomingMessage event) {
    replyHelper.dispatch(event, "悬赏放弃", mapCommandHandler::handleAbandonBounty);
  }
}
