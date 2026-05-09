package top.stillmisty.xiantao.handle.qq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.component.qguild.message.QGMarkdown;
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
public class QQHelpHandle {

  private final HelpCommandHandler helpCommandHandler;
  private final NotificationAppender notificationAppender;

  @Listener
  @ContentTrim
  @Filter("帮助")
  public void help(QGGroupAtMessageCreateEvent event) {
    log.debug("收到帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        helpCommandHandler.handleHelpMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), null);
    sendWithNotifications(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("帮助 {{command}}")
  public void helpDetail(
      QGGroupAtMessageCreateEvent event, @FilterValue("command") String command) {
    log.debug("收到命令详情请求 - AuthorId: {}, Command: {}", event.getAuthorId(), command);
    String response =
        helpCommandHandler.handleHelpMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), command);
    sendWithNotifications(event, response);
  }

  private void sendWithNotifications(QGGroupAtMessageCreateEvent event, String response) {
    var result =
        notificationAppender.prepareAppend(
            PlatformType.QQ, event.getAuthorId().toString(), response);
    event.replyBlocking(QGMarkdown.create(result.text()));
    notificationAppender.markDelivered(result.eventIds());
  }
}
