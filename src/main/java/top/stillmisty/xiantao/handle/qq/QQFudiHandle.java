package top.stillmisty.xiantao.handle.qq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class QQFudiHandle {

  private final FudiCommandHandler fudiCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudi(QGGroupAtMessageCreateEvent event) {
    log.debug("收到福地请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiStatusMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGrid(QGGroupAtMessageCreateEvent event) {
    log.debug("收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiGridMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpirit(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    log.debug("收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    String response =
        fudiCommandHandler.handleSpiritChatMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), content);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地种植 {{position}} {{cropName}}")
  public void handlePlant(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("cropName") String cropName) {
    log.debug(
        "收到种植请求 - AuthorId: {}, Position: {}, CropName: {}",
        event.getAuthorId(),
        position,
        cropName);
    String response =
        fudiCommandHandler.handlePlantMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position, cropName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地收取 {{position}}")
  public void handleCollect(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到收取请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleCollectMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地建造 {{position}} {{cellType}}")
  public void handleBuild(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("cellType") String cellType) {
    log.debug(
        "收到建造请求 - AuthorId: {}, Position: {}, CellType: {}",
        event.getAuthorId(),
        position,
        cellType);
    String response =
        fudiCommandHandler.handleBuildMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position, cellType);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地拆除 {{position}}")
  public void handleRemove(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到拆除请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleRemoveMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地升级 {{position}}")
  public void handleUpgradeCell(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到升级请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleUpgradeCellMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地孵化 {{position}} {{eggName}}")
  public void handleHatch(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("eggName") String eggName) {
    log.debug(
        "收到孵化请求 - AuthorId: {}, Position: {}, EggName: {}", event.getAuthorId(), position, eggName);
    String response =
        fudiCommandHandler.handleHatchMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position, eggName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地放生 {{position}}")
  public void handleRelease(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleReleaseMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地进化 {{position}} {{mode}}")
  public void handleEvolve(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("mode") String mode) {
    log.debug("收到进化请求 - AuthorId: {}, Position: {}, Mode: {}", event.getAuthorId(), position, mode);
    String response =
        fudiCommandHandler.handleEvolveMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position, mode);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵送礼 {{itemName}}")
  public void handleGiveGift(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到送礼请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        fudiCommandHandler.handleGiveGiftMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), itemName);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(QGGroupAtMessageCreateEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, event.getAuthorId().toString(), response);
    event.replyBlocking(QGMarkdown.create(result.text()));
    notificationAppender.markDelivered(result.eventIds());
  }
}
