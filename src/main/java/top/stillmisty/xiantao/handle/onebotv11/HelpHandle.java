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
import top.stillmisty.xiantao.handle.command.HelpCommandHandler;
import top.stillmisty.xiantao.service.NotificationAppender;

@Slf4j
@Component
@RequiredArgsConstructor
public class HelpHandle {

  private final HelpCommandHandler helpCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("帮助")
  public void help(OneBotMessageEvent event) {
    log.debug("收到帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        helpCommandHandler.handleHelp(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), null);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("帮助 {{command}}")
  public void helpDetail(OneBotMessageEvent event, @FilterValue("command") String command) {
    log.debug("收到命令详情请求 - AuthorId: {}, Command: {}", event.getAuthorId(), command);
    String response =
        helpCommandHandler.handleHelp(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), command);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(OneBotMessageEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), response);
    event.replyBlocking(result.text());
    notificationAppender.markDelivered(result.eventIds());
  }
}
