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
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

/**
 * 修仙系统监听器
 * 处理加点、洗点、突破、护道等修仙相关命令
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CultivationHandle {

    private final CultivationCommandHandler cultivationCommandHandler;

    /**
     * 处理突破命令
     * 格式：突破
     */
    @Listener
    @ContentTrim
    @Filter("突破")
    public void breakthrough(MessageEvent event) {
        log.debug("收到突破请求 - AuthorId: {}", event.getAuthorId());

        String response = cultivationCommandHandler.handleBreakthrough(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }

    /**
     * 处理护道命令
     * 格式：护道 [道号]
     * 示例：护道 张三
     */
    @Listener
    @ContentTrim
    @Filter("护道 {{nickname}}")
    public void establishProtection(MessageEvent event, @FilterValue("nickname") String nickname) {
        log.debug("收到护道请求 - AuthorId: {}, Content: {}", event.getAuthorId(), nickname);

        String response = cultivationCommandHandler.handleEstablishProtection(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                nickname
        );

        event.replyBlocking(response);
    }

    /**
     * 处理护道解除命令
     * 格式：护道解除 [道号]
     * 示例：护道解除 张三
     */
    @Listener
    @ContentTrim
    @Filter("护道解除 {{nickname}}")
    public void removeProtection(MessageEvent event, @FilterValue("nickname") String nickname) {
        log.debug("收到护道解除请求 - AuthorId: {}, Content: {}", event.getAuthorId(), nickname);

        String response = cultivationCommandHandler.handleRemoveProtection(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString(),
                nickname
        );

        event.replyBlocking(response);
    }

    /**
     * 处理护道查询命令
     * 格式：护道查询
     */
    @Listener
    @ContentTrim
    @Filter("护道查询")
    public void queryProtection(MessageEvent event) {
        log.debug("收到护道查询请求 - AuthorId: {}", event.getAuthorId());
        String response = cultivationCommandHandler.handleQueryProtection(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );
        event.replyBlocking(response);
    }
}
