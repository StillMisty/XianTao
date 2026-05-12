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
    String response =
        mapCommandHandler.handleCurrentMap(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地图列表")
  public void mapList(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleMapList(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("前往 {{mapName}}")
  public void goTo(OneBotMessageEvent event, @FilterValue("mapName") String mapName) {
    String response =
        mapCommandHandler.handleGoTo(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), mapName);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练")
  public void training(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleTraining(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练结算")
  public void endTraining(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleEndTraining(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏列表")
  public void bountyList(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleBountyList(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏")
  public void bountyStatus(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleBountyStatus(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏接取 {{bountyId}}")
  public void startBounty(OneBotMessageEvent event, @FilterValue("bountyId") String bountyId) {
    String response =
        mapCommandHandler.handleStartBounty(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), bountyId);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏结算")
  public void completeBounty(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleCompleteBounty(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏放弃")
  public void abandonBounty(OneBotMessageEvent event) {
    String response =
        mapCommandHandler.handleAbandonBounty(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("地图")
  public void currentMapQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleCurrentMapMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地图列表")
  public void mapListQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleMapListMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("前往 {{mapName}}")
  public void goToQq(QGGroupAtMessageCreateEvent event, @FilterValue("mapName") String mapName) {
    String response =
        mapCommandHandler.handleGoToMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), mapName);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练")
  public void trainingQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleTrainingMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("历练结算")
  public void endTrainingQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleEndTrainingMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏列表")
  public void bountyListQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleBountyListMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏")
  public void bountyStatusQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleBountyStatusMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏接取 {{bountyId}}")
  public void startBountyQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("bountyId") String bountyId) {
    String response =
        mapCommandHandler.handleStartBountyMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), bountyId);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏结算")
  public void completeBountyQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleCompleteBountyMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("悬赏放弃")
  public void abandonBountyQq(QGGroupAtMessageCreateEvent event) {
    String response =
        mapCommandHandler.handleAbandonBountyMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }
}
