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
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleUndeployBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleRecoverBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
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
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, mode);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleReleaseBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("出战灵兽")
  public void getDeployedBeasts(OneBotMessageEvent event) {
    log.debug("收到查看出战灵兽请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleGetDeployedBeasts(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
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
        beastCommandHandler.handleDeployBeastMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleUndeployBeastMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleRecoverBeastMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
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
        beastCommandHandler.handleEvolveBeastMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position, mode);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeastQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleReleaseBeastMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("出战灵兽")
  public void getDeployedBeastsQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到查看出战灵兽请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleGetDeployedBeastsMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }
}
