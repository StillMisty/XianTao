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

/** 查看系统监听器 处理查看玩家信息命令 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewHandle {

  private final CultivationCommandHandler commandHandler;

  @Listener
  @ContentTrim
  @Filter("查看 {{targetNickname}}")
  public void viewPlayer(MessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    log.debug("收到查看请求 - AuthorId: {}, Target: {}", event.getAuthorId(), targetNickname);
    String response =
        commandHandler.handleViewPlayer(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), targetNickname);
    event.replyBlocking(response);
  }
}
