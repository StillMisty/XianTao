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
    @Filter("福地网格")
    public void handleFudiGrid(MessageEvent event) {
        log.debug("收到福地网格请求 - AuthorId: {}", event.getAuthorId());

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

    /**
     * 种植 <坐标> <作物名称>
     */
    @Listener
    @ContentTrim
    @Filter("种植 {{position}} {{cropName}}")
    public void handlePlant(MessageEvent event, @FilterValue("position") String position, @FilterValue("cropName") String cropName) {
        log.debug(
                "收到种植请求 - AuthorId: {}, Position: {}, CropName: {}",
                event.getAuthorId(), position, cropName
        );

        String response = fudiCommandHandler.handlePlant(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position,
                cropName
        );

        event.replyBlocking(response);
    }

    /**
     * 收获 <坐标> 或 收获 all
     */
    @Listener
    @ContentTrim
    @Filter("收获 {{position}}")
    public void handleHarvest(MessageEvent event, @FilterValue("position") String position) {
        log.debug("收到收获请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);

        String response = fudiCommandHandler.handleHarvest(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position
        );

        event.replyBlocking(response);
    }

    /**
     * 建造 <坐标> <地块类型>
     */
    @Listener
    @ContentTrim
    @Filter("建造 {{position}} {{cellType}}")
    public void handleBuild(MessageEvent event, @FilterValue("position") String position, @FilterValue("cellType") String cellType) {
        log.debug(
                "收到建造请求 - AuthorId: {}, Position: {}, CellType: {}",
                event.getAuthorId(), position, cellType
        );

        String response = fudiCommandHandler.handleBuild(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position,
                cellType
        );

        event.replyBlocking(response);
    }

    /**
     * 拆除 <坐标>
     */
    @Listener
    @ContentTrim
    @Filter("拆除 {{position}}")
    public void handleRemove(MessageEvent event, @FilterValue("position") String position) {
        log.debug("收到拆除请求 - AuthorId: {}, Position: {}", event.getAuthorId(), position);

        String response = fudiCommandHandler.handleRemove(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position
        );

        event.replyBlocking(response);
    }

    /**
     * 献祭 <物品名称> 或 #献祭 all
     */
    @Listener
    @ContentTrim
    @Filter("献祭 {{itemName}}")
    public void handleSacrifice(MessageEvent event, @FilterValue("itemName") String itemName) {
        log.debug("收到献祭请求 - AuthorId: {}, ItemName: {}", event.getAuthorId(), itemName);

        String response = fudiCommandHandler.handleSacrifice(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                itemName
        );

        event.replyBlocking(response);
    }

    /**
     * 喂养 <坐标> <饲料名称>
     */
    @Listener
    @ContentTrim
    @Filter("喂养 {{position}} {{feedItem}}")
    public void handleFeed(MessageEvent event, @FilterValue("position") String position, @FilterValue("feedItem") String feedItem) {
        log.debug(
                "收到喂养请求 - AuthorId: {}, Position: {}, FeedItem: {}",
                event.getAuthorId(), position, feedItem
        );

        String response = fudiCommandHandler.handleFeed(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                position,
                feedItem
        );

        event.replyBlocking(response);
    }

    /**
     * 福地自动 <开/关>
     */
    @Listener
    @ContentTrim
    @Filter("#福地自动 {{mode}}")
    public void handleAutoMode(MessageEvent event, @FilterValue("mode") String mode) {
        log.debug("收到福地自动请求 - AuthorId: {}, Mode: {}", event.getAuthorId(), mode);

        String response = fudiCommandHandler.handleAutoMode(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                mode
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地升级")
    public void handleUpgrade(MessageEvent event) {
        log.debug("收到福地升级请求 - AuthorId: {}", event.getAuthorId());

        String response = fudiCommandHandler.handleUpgrade(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    @Listener
    @ContentTrim
    @Filter("#福地扩建")
    public void handleExpand(MessageEvent event) {
        log.debug("收到福地扩建请求 - AuthorId: {}", event.getAuthorId());

        String response = fudiCommandHandler.handleExpand(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }
}
