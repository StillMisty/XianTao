package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;

/**
 * 福地系统监听器
 * 处理福地相关的跨平台命令
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FudiHandle {

    private final FudiCommandHandler fudiCommandHandler;

    @Listener
    @ContentTrim
    @Filter("福地")
    public void handleFudi(MessageEvent event) {
        log.debug("收到福地请求 - AuthorId: {}", event.getAuthorId());

        String response = fudiCommandHandler.handleFudiStatus(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("福地地块")
    public void handleFudiGrid(MessageEvent event) {
        log.debug("收到福地地块请求 - AuthorId: {}", event.getAuthorId());

        String response = fudiCommandHandler.handleFudiGrid(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("福地灵气")
    public void handleFudiAura(MessageEvent event) {
        log.debug("收到福地灵气请求 - AuthorId: {}", event.getAuthorId());

        String response = fudiCommandHandler.handleFudiAura(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("地灵 {{content}}")
    public void handleFudiSpirit(MessageEvent event, @FilterValue("content") String content) {
        log.debug("收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);

        String response = fudiCommandHandler.handleSpiritChat(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                content
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地种植 {{position}} {{cropName}}")
    public void handlePlant(MessageEvent event, @FilterValue("position") String position, @FilterValue("cropName") String cropName) {
        log.debug("收到种植请求 - AuthorId: {}, Position: {}, CropName: {}", event.getAuthorId(), position, cropName);

        String response = fudiCommandHandler.handlePlant(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position,
                cropName
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地收获 {{position}}")
    public void handleHarvest(MessageEvent event, @FilterValue("position") String position) {
        log.debug("收到收获请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);

        String response = fudiCommandHandler.handleHarvest(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地建造 {{position}} {{cellType}}")
    public void handleBuild(MessageEvent event, @FilterValue("position") String position, @FilterValue("cellType") String cellType) {
        log.debug("收到建造请求 - AuthorId: {}, Position: {}, CellType: {}", event.getAuthorId(), position, cellType);

        String response = fudiCommandHandler.handleBuild(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position,
                cellType
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地拆除 {{position}}")
    public void handleRemove(MessageEvent event, @FilterValue("position") String position) {
        log.debug("收到拆除请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);

        String response = fudiCommandHandler.handleRemove(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地献祭 {{itemName}}")
    public void handleSacrifice(MessageEvent event, @FilterValue("itemName") String itemName) {
        log.debug("收到献祭请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);

        String response = fudiCommandHandler.handleSacrifice(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                itemName
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地升级 {{position}}")
    public void handleUpgradeCell(MessageEvent event, @FilterValue("position") String position) {
        log.debug("收到升级请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);

        String response = fudiCommandHandler.handleUpgradeCell(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地孵化 {{position}} {{eggName}}")
    public void handleHatch(MessageEvent event, @FilterValue("position") String position, @FilterValue("eggName") String eggName) {
        log.debug("收到孵化请求 - AuthorId: {}, Position: {}, EggName: {}", event.getAuthorId(), position, eggName);

        String response = fudiCommandHandler.handleHatch(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position,
                eggName
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地收取 {{position}}")
    public void handleCollect(MessageEvent event, @FilterValue("position") String position) {
        log.debug("收到收取请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);

        String response = fudiCommandHandler.handleCollect(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地放生 {{position}}")
    public void handleRelease(MessageEvent event, @FilterValue("position") String position) {
        log.debug("收到放生请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);

        String response = fudiCommandHandler.handleRelease(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地进化 {{position}} {{mode}}")
    public void handleEvolve(MessageEvent event, @FilterValue("position") String position, @FilterValue("mode") String mode) {
        log.debug("收到进化请求 - AuthorId: {}, Position: {}, Mode: {}", event.getAuthorId(), position, mode);

        String response = fudiCommandHandler.handleEvolve(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position,
                mode
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#地灵送礼 {{itemName}}")
    public void handleGiveGift(MessageEvent event, @FilterValue("itemName") String itemName) {
        log.debug("收到送礼请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);

        String response = fudiCommandHandler.handleGiveGift(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                itemName
        );

        event.replyBlocking(response);
    }


}
