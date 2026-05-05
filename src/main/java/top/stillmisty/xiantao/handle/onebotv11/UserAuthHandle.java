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
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

/** 用户注册监听器 处理「我要修仙」命令 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthHandle {

  private final CultivationCommandHandler commandHandler;

  @Listener
  @ContentTrim
  @Filter("我要修仙 {{nickname}}")
  public void register(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.info("收到注册请求 - AuthorId: {}, Nickname: {}", event.getAuthorId(), nickname);

    String response =
        commandHandler.handleRegister(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname);

    event.replyBlocking(response);
  }
}
