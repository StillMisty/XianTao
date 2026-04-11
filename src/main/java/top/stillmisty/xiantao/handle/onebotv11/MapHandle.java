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
import top.stillmisty.xiantao.handle.command.MapCommandHandler;

/**
 * 地图管理监听器
 * 处理地图、旅行、历练、探索相关命令
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MapHandle {

    private final MapCommandHandler mapCommandHandler;

    /**
     * 处理地图列表命令
     */
    @Listener
    @ContentTrim
    @Filter("地图")
    public void mapList(MessageEvent event) {
        log.debug("收到地图列表请求 - AuthorId: {}", event.getAuthorId());

        String response = mapCommandHandler.handleMapList(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    /**
     * 处理前往命令
     * 优先使用体力，体力不足时使用真实时间
     */
    @Listener
    @ContentTrim
    @Filter("前往 {{mapName}}")
    public void goTo(MessageEvent event, @FilterValue("mapName") String mapName) {
        log.debug("收到前往请求 - AuthorId: {}, MapName: {}", event.getAuthorId(), mapName);

        String response = mapCommandHandler.handleGoTo(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                mapName,
                false // 自动判断是否使用体力
        );

        event.replyBlocking(response);
    }

    /**
     * 处理前往命令（强制使用真实时间）
     */
    @Listener
    @ContentTrim
    @Filter("前往 {{mapName}} 等待")
    public void goToWait(MessageEvent event, @FilterValue("mapName") String mapName) {
        log.debug("收到前往请求（等待模式）- AuthorId: {}, MapName: {}", event.getAuthorId(), mapName);

        String response = mapCommandHandler.handleGoTo(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                mapName,
                true // 强制使用真实时间模式
        );

        event.replyBlocking(response);
    }

    /**
     * 处理历练命令
     */
    @Listener
    @ContentTrim
    @Filter("历练")
    public void training(MessageEvent event) {
        log.debug("收到历练请求 - AuthorId: {}", event.getAuthorId());

        String response = mapCommandHandler.handleTraining(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    /**
     * 处理结束历练命令
     */
    @Listener
    @ContentTrim
    @Filter("历练结束")
    public void endTraining(MessageEvent event) {
        log.debug("收到结束历练请求 - AuthorId: {}", event.getAuthorId());

        String response = mapCommandHandler.handleEndTraining(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    /**
     * 处理探索命令
     */
    @Listener
    @ContentTrim
    @Filter("探索")
    public void explore(MessageEvent event) {
        log.debug("收到探索请求 - AuthorId: {}", event.getAuthorId());

        String response = mapCommandHandler.handleExplore(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }
}
