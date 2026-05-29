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
import top.stillmisty.xiantao.handle.command.MapCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapListener {
  private final MapCommandHandler mapCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("地图")
  public void currentMap(OneBotMessageEvent event) {
    replyHelper.oneBot(event, mapCommandHandler::handleMap);
  }

  @Listener
  @ContentTrim
  @Filter("前往\\s*{{mapName}}")
  public void goTo(OneBotMessageEvent event, @FilterValue("mapName") String mapName) {
    replyHelper.oneBot(event, mapName, mapCommandHandler::handleGoTo);
  }

  @Listener
  @ContentTrim
  @Filter("历练")
  public void training(OneBotMessageEvent event) {
    replyHelper.oneBot(event, mapCommandHandler::handleTraining);
  }

  @Listener
  @ContentTrim
  @Filter("历练结算")
  public void endTraining(OneBotMessageEvent event) {
    replyHelper.oneBot(event, mapCommandHandler::handleEndTraining);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏")
  public void bounty(OneBotMessageEvent event) {
    replyHelper.oneBot(event, mapCommandHandler::handleBounty);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏接取\\s*{{bountyId}}")
  public void startBounty(OneBotMessageEvent event, @FilterValue("bountyId") String bountyId) {
    replyHelper.oneBot(event, bountyId, mapCommandHandler::handleStartBounty);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏结算")
  public void completeBounty(OneBotMessageEvent event) {
    replyHelper.oneBot(event, mapCommandHandler::handleCompleteBounty);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏放弃")
  public void abandonBounty(OneBotMessageEvent event) {
    replyHelper.oneBot(event, mapCommandHandler::handleAbandonBounty);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("地图")
  public void currentMapQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, mapCommandHandler::handleMap);
  }

  @Listener
  @ContentTrim
  @Filter("前往\\s*{{mapName}}")
  public void goToQq(QGGroupAtMessageCreateEvent event, @FilterValue("mapName") String mapName) {
    replyHelper.qq(event, mapName, mapCommandHandler::handleGoTo);
  }

  @Listener
  @ContentTrim
  @Filter("历练")
  public void trainingQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, mapCommandHandler::handleTraining);
  }

  @Listener
  @ContentTrim
  @Filter("历练结算")
  public void endTrainingQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, mapCommandHandler::handleEndTraining);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏")
  public void bountyQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, mapCommandHandler::handleBounty);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏接取\\s*{{bountyId}}")
  public void startBountyQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("bountyId") String bountyId) {
    replyHelper.qq(event, bountyId, mapCommandHandler::handleStartBounty);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏结算")
  public void completeBountyQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, mapCommandHandler::handleCompleteBounty);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏放弃")
  public void abandonBountyQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, mapCommandHandler::handleAbandonBounty);
  }
}
