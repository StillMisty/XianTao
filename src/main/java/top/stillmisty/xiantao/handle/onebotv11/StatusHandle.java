package top.stillmisty.xiantao.handle.onebotv11;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

/**
 * 状态查询监听器
 * 处理「状态」命令
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatusHandle {

    private final CultivationCommandHandler commandHandler;

    @Listener
    @ContentTrim
    @Filter("状态")
    public void status(MessageEvent event) {
        log.debug("收到状态查询请求 - AuthorId: {}", event.getAuthorId());

        String response = commandHandler.handleStatus(
                PlatformType.ONE_BOT_V11,
                event.getAuthorId().toString()
        );

        event.replyBlocking(response);
    }
}
