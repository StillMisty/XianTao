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
import top.stillmisty.xiantao.handle.TextFormat;
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
    log.debug("收到灵兽出战请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleDeployBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleUndeployBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleRecoverBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽进化 {{position}} {{mode}}")
  public void evolveBeast(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("mode") String mode) {
    log.debug(
        "收到灵兽进化请求 - AuthorId: {}, Position: {}, Mode: {}", event.getAuthorId(), position, mode);
    String response =
        beastCommandHandler.handleEvolveBeast(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            position,
            mode,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleReleaseBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽")
  public void handleBeast(OneBotMessageEvent event) {
    log.debug("收到灵兽请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleGetDeployedBeasts(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽列表")
  public void handleBeastList(OneBotMessageEvent event) {
    log.debug("收到灵兽列表请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleBeastList(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽喂养 {{position}} {{quantity}}")
  public void feedBeast(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("quantity") int quantity) {
    log.debug(
        "收到灵兽喂养请求 - AuthorId: {}, Position: {}, Quantity: {}",
        event.getAuthorId(),
        position,
        quantity);
    String response =
        beastCommandHandler.handleFeedBeast(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            position,
            quantity,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("灵兽出战 {{position}}")
  public void deployBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽出战请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleDeployBeast(
            PlatformType.QQ, event.getAuthorId().toString(), position, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleUndeployBeast(
            PlatformType.QQ, event.getAuthorId().toString(), position, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleRecoverBeast(
            PlatformType.QQ, event.getAuthorId().toString(), position, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽进化 {{position}} {{mode}}")
  public void evolveBeastQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("mode") String mode) {
    log.debug(
        "收到灵兽进化请求 - AuthorId: {}, Position: {}, Mode: {}", event.getAuthorId(), position, mode);
    String response =
        beastCommandHandler.handleEvolveBeast(
            PlatformType.QQ, event.getAuthorId().toString(), position, mode, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleReleaseBeast(
            PlatformType.QQ, event.getAuthorId().toString(), position, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽")
  public void handleBeastQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到灵兽请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleGetDeployedBeasts(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽列表")
  public void handleBeastListQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到灵兽列表请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleBeastList(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽喂养 {{position}} {{quantity}}")
  public void feedBeastQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("quantity") int quantity) {
    log.debug(
        "收到灵兽喂养请求 - AuthorId: {}, Position: {}, Quantity: {}",
        event.getAuthorId(),
        position,
        quantity);
    String response =
        beastCommandHandler.handleFeedBeast(
            PlatformType.QQ,
            event.getAuthorId().toString(),
            position,
            quantity,
            TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }
}
