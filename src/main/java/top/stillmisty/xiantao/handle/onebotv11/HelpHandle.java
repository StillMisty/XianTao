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
import top.stillmisty.xiantao.handle.command.HelpCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class HelpHandle {

  private final HelpCommandHandler helpCommandHandler;

  @Listener
  @ContentTrim
  @Filter("帮助")
  public void help(MessageEvent event) {
    log.debug("收到帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        helpCommandHandler.handleHelp(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), null);
    event.replyBlocking(response);
  }

  @Listener
  @ContentTrim
  @Filter("帮助 {{command}}")
  public void helpDetail(MessageEvent event, @FilterValue("command") String command) {
    log.debug("收到命令详情请求 - AuthorId: {}, Command: {}", event.getAuthorId(), command);
    String response =
        helpCommandHandler.handleHelp(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), command);
    event.replyBlocking(response);
  }
}
