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
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class FudiListener {

  private final FudiCommandHandler fudiCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudi(OneBotMessageEvent event) {
    log.debug("收到福地请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiStatus(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGrid(OneBotMessageEvent event) {
    log.debug("收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiGrid(PlatformType.ONE_BOT_V11, event.getAuthorId().toString());
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpirit(OneBotMessageEvent event, @FilterValue("content") String content) {
    log.debug("收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    String response =
        fudiCommandHandler.handleSpiritChat(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), content);
    replyHelper.replyOneBot(event, response);
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
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地收取 {{position}}")
  public void handleCollect(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到收取请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleCollect(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    replyHelper.replyOneBot(event, response);
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
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地拆除 {{position}}")
  public void handleRemove(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到拆除请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleRemove(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    replyHelper.replyOneBot(event, response);
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
    replyHelper.replyOneBot(event, response);
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
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地放生 {{position}}")
  public void handleRelease(OneBotMessageEvent event, @FilterValue("position") String position) {
    log.debug("收到放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleRelease(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), position);
    replyHelper.replyOneBot(event, response);
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
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵送礼 {{itemName}}")
  public void handleGiveGift(OneBotMessageEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到送礼请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        fudiCommandHandler.handleGiveGift(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), itemName);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudiQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到福地请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiStatusMarkdown(
            PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGridQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiGridMarkdown(PlatformType.QQ, event.getAuthorId().toString());
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpiritQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    log.debug("收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    String response =
        fudiCommandHandler.handleSpiritChatMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), content);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地种植 {{position}} {{cropName}}")
  public void handlePlantQq(
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
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地收取 {{position}}")
  public void handleCollectQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到收取请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleCollectMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地建造 {{position}} {{cellType}}")
  public void handleBuildQq(
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
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地拆除 {{position}}")
  public void handleRemoveQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到拆除请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleRemoveMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地升级 {{position}}")
  public void handleUpgradeCellQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到升级请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleUpgradeCellMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地孵化 {{position}} {{eggName}}")
  public void handleHatchQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("eggName") String eggName) {
    log.debug(
        "收到孵化请求 - AuthorId: {}, Position: {}, EggName: {}", event.getAuthorId(), position, eggName);
    String response =
        fudiCommandHandler.handleHatchMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position, eggName);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地放生 {{position}}")
  public void handleReleaseQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("position") String position) {
    log.debug("收到放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);
    String response =
        fudiCommandHandler.handleReleaseMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地进化 {{position}} {{mode}}")
  public void handleEvolveQq(
      QGGroupAtMessageCreateEvent event,
      @FilterValue("position") String position,
      @FilterValue("mode") String mode) {
    log.debug("收到进化请求 - AuthorId: {}, Position: {}, Mode: {}", event.getAuthorId(), position, mode);
    String response =
        fudiCommandHandler.handleEvolveMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), position, mode);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵送礼 {{itemName}}")
  public void handleGiveGiftQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("itemName") String itemName) {
    log.debug("收到送礼请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);
    String response =
        fudiCommandHandler.handleGiveGiftMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), itemName);
    replyHelper.replyQQ(event, response);
  }
}
