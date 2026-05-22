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
import top.stillmisty.xiantao.handle.command.BeastCommandHandler;

@Slf4j
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
    log.debug("[OneBot] 收到灵兽出战请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.oneBot(event, position, beastCommandHandler::handleDeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("[OneBot] 收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.oneBot(event, position, beastCommandHandler::handleUndeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("[OneBot] 收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.oneBot(event, position, beastCommandHandler::handleRecoverBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽进化 {{position}}")
  public void evolveBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("[OneBot] 收到灵兽进化请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.oneBot(event, position, beastCommandHandler::handleEvolveBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("[OneBot] 收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.oneBot(event, position, beastCommandHandler::handleReleaseBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽")
  public void handleBeast(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到灵兽请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, beastCommandHandler::handleGetDeployedBeasts);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽列表")
  public void handleBeastList(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到灵兽列表请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, beastCommandHandler::handleBeastList);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽喂养 {{position}} {{quantity}}")
  public void feedBeast(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("quantity") int quantity) {
    log.debug(
        "[OneBot] 收到灵兽喂养请求 - AuthorId: {}, Position: {}, Quantity: {}",
        event.getAuthorId(),
        position,
        quantity);
    replyHelper.oneBot(
        event, (p, o, f) -> beastCommandHandler.handleFeedBeast(p, o, position, quantity, f));
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("灵兽出战 {{position}}")
  public void deployBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("[QQ] 收到灵兽出战请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.qq(event, position, beastCommandHandler::handleDeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("[QQ] 收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.qq(event, position, beastCommandHandler::handleUndeployBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("[QQ] 收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.qq(event, position, beastCommandHandler::handleRecoverBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽进化 {{position}}")
  public void evolveBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("[QQ] 收到灵兽进化请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.qq(event, position, beastCommandHandler::handleEvolveBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("[QQ] 收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    replyHelper.qq(event, position, beastCommandHandler::handleReleaseBeast);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽")
  public void handleBeastQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到灵兽请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, beastCommandHandler::handleGetDeployedBeasts);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽列表")
  public void handleBeastListQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到灵兽列表请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, beastCommandHandler::handleBeastList);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽喂养 {{position}} {{quantity}}")
  public void feedBeastQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("quantity") int quantity) {
    log.debug(
        "[QQ] 收到灵兽喂养请求 - AuthorId: {}, Position: {}, Quantity: {}",
        event.getAuthorId(),
        position,
        quantity);
    replyHelper.qq(
        event, (p, o, f) -> beastCommandHandler.handleFeedBeast(p, o, position, quantity, f));
  }
}
