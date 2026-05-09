package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.BeastCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeastHandle {

  private final BeastCommandHandler beastCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("灵兽出战 {{position}}")
  public void deployBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽出战请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleDeployBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽召回 {{position}}")
  public void undeployBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽召回请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleUndeployBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽恢复 {{position}}")
  public void recoverBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽恢复请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleRecoverBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
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
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("灵兽放生 {{position}}")
  public void releaseBeast(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到灵兽放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        beastCommandHandler.handleReleaseBeast(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("出战灵兽")
  public void getDeployedBeasts(OneBotMessageEvent event) {
    log.debug("收到查看出战灵兽请求 - AuthorId: {}", event.getAuthorId());
    String response =
        beastCommandHandler.handleGetDeployedBeasts(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(OneBotMessageEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), response);
    event.replyBlocking(result.text());
    notificationAppender.markDelivered(result.eventIds());
  }
}
