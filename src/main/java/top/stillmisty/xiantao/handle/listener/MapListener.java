package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.MapCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class MapListener {
  private final MapCommandHandler mapCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "地图")
  public void currentMap(MessageEvent event) {
    replyHelper.dispatch(event, "地图", mapCommandHandler::handleMap);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "前往\\s*{{mapName}}")
  public void goTo(MessageEvent event, @FilterValue("mapName") String mapName) {
    replyHelper.dispatch(event, "前往", mapName, mapCommandHandler::handleGoTo);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "历练")
  public void training(MessageEvent event) {
    replyHelper.dispatch(event, "历练", mapCommandHandler::handleTraining);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "历练结算")
  public void endTraining(MessageEvent event) {
    replyHelper.dispatch(event, "历练结算", mapCommandHandler::handleEndTraining);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "悬赏")
  public void bounty(MessageEvent event) {
    replyHelper.dispatch(event, "悬赏", mapCommandHandler::handleBounty);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "悬赏接取\\s*{{bountyId}}")
  public void startBounty(MessageEvent event, @FilterValue("bountyId") String bountyId) {
    replyHelper.dispatch(event, "悬赏接取", bountyId, mapCommandHandler::handleStartBounty);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "悬赏结算")
  public void completeBounty(MessageEvent event) {
    replyHelper.dispatch(event, "悬赏结算", mapCommandHandler::handleCompleteBounty);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "悬赏放弃")
  public void abandonBounty(MessageEvent event) {
    replyHelper.dispatch(event, "悬赏放弃", mapCommandHandler::handleAbandonBounty);
  }
}
