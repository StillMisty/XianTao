package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.BeastCommandHandler;

@Component
@RequiredArgsConstructor
public class BeastListener {

  private final BeastCommandHandler beastCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("灵兽出战 {{position}}")
  public void deployBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    replyHelper.oneBot(event, "灵兽出战", position, beastCommandHandler::handleDeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    replyHelper.oneBot(event, "灵兽召回", position, beastCommandHandler::handleUndeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    replyHelper.oneBot(event, "灵兽恢复", position, beastCommandHandler::handleRecoverBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽进化 {{position}}")
  public void evolveBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    replyHelper.oneBot(event, "灵兽进化", position, beastCommandHandler::handleEvolveBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    replyHelper.oneBot(event, "灵兽放生", position, beastCommandHandler::handleReleaseBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽")
  public void handleBeast(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "灵兽", beastCommandHandler::handleGetDeployedBeasts);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽列表")
  public void handleBeastList(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "灵兽列表", beastCommandHandler::handleBeastList);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽喂养 {{position}} {{quantity}}")
  public void feedBeast(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("quantity") int quantity) {
    replyHelper.oneBot(
        event,
        "灵兽喂养",
        (p, o, f) -> beastCommandHandler.handleFeedBeast(p, o, position, quantity, f));
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("灵兽出战 {{position}}")
  public void deployBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    replyHelper.qq(event, "灵兽出战", position, beastCommandHandler::handleDeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    replyHelper.qq(event, "灵兽召回", position, beastCommandHandler::handleUndeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    replyHelper.qq(event, "灵兽恢复", position, beastCommandHandler::handleRecoverBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽进化 {{position}}")
  public void evolveBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    replyHelper.qq(event, "灵兽进化", position, beastCommandHandler::handleEvolveBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    replyHelper.qq(event, "灵兽放生", position, beastCommandHandler::handleReleaseBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽")
  public void handleBeastQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "灵兽", beastCommandHandler::handleGetDeployedBeasts);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽列表")
  public void handleBeastListQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "灵兽列表", beastCommandHandler::handleBeastList);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽喂养 {{position}} {{quantity}}")
  public void feedBeastQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("quantity") int quantity) {
    replyHelper.qq(
        event,
        "灵兽喂养",
        (p, o, f) -> beastCommandHandler.handleFeedBeast(p, o, position, quantity, f));
  }
}
