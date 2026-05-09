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
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class FudiHandle {

  private final FudiCommandHandler fudiCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudi(OneBotMessageEvent event) {
    log.debug("收到福地请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiStatus(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGrid(OneBotMessageEvent event) {
    log.debug("收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiGrid(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpirit(OneBotMessageEvent event, @FilterValue("content") String content) {
    log.debug("收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    String response =
        fudiCommandHandler.handleSpiritChat(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), content);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地种植 {{position}} {{cropName}}")
  public void handlePlant(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("cropName") String cropName) {
    log.debug(
        "收到种植请求 - AuthorId: {}, Position: {}, CropName: {}",
        event.getAuthorId(),
        position,
        cropName);
    String response =
        fudiCommandHandler.handlePlant(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, cropName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地收取 {{position}}")
  public void handleCollect(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到收取请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleCollect(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地建造 {{position}} {{cellType}}")
  public void handleBuild(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("cellType") String cellType) {
    log.debug(
        "收到建造请求 - AuthorId: {}, Position: {}, CellType: {}",
        event.getAuthorId(),
        position,
        cellType);
    String response =
        fudiCommandHandler.handleBuild(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, cellType);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地拆除 {{position}}")
  public void handleRemove(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到拆除请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleRemove(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地升级 {{position}}")
  public void handleUpgradeCell(
      OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到升级请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleUpgradeCell(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地孵化 {{position}} {{eggName}}")
  public void handleHatch(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("eggName") String eggName) {
    log.debug(
        "收到孵化请求 - AuthorId: {}, Position: {}, EggName: {}", event.getAuthorId(), position, eggName);
    String response =
        fudiCommandHandler.handleHatch(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, eggName);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地放生 {{position}}")
  public void handleRelease(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleRelease(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地进化 {{position}} {{mode}}")
  public void handleEvolve(
      OneBotMessageEvent event,
      @FilterValue("position") String position,
      @FilterValue("mode") String mode) {
    log.debug("收到进化请求 - AuthorId: {}, Position: {}, Mode: {}", event.getAuthorId(), position, mode);
    String response =
        fudiCommandHandler.handleEvolve(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position, mode);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵送礼 {{itemName}}")
  public void handleGiveGift(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到送礼请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        fudiCommandHandler.handleGiveGift(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);
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
