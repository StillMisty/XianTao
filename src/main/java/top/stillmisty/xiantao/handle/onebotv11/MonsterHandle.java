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
import top.stillmisty.xiantao.handle.command.MonsterCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonsterHandle {

    private final MonsterCommandHandler monsterCommandHandler;

    @Listener
    @ContentTrim
    @Filter("战斗模拟 {{durationMinutes}}")
    public void simulateDuel(MessageEvent event, @FilterValue("durationMinutes") String durationMinutes) {
        try {
            int minutes = Integer.parseInt(durationMinutes);
            String response = monsterCommandHandler.handleSimulateDuel(
                    PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), minutes);
            event.replyBlocking(response);
        } catch (NumberFormatException e) {
            event.replyBlocking("请输入有效的分钟数");
        }
    }
}
