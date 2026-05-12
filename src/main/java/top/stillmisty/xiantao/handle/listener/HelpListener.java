package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
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
public class HelpListener {

  private final HelpCommandHandler helpCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("帮助")
  public void help(OneBotMessageEvent event) {
    log.debug("收到帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        helpCommandHandler.handleHelp(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), null);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("帮助 {{command}}")
  public void helpDetail(OneBotMessageEvent event, @FilterValue("command") String command) {
    log.debug("收到命令详情请求 - AuthorId: {}, Command: {}", event.getAuthorId(), command);
    String response =
        helpCommandHandler.handleHelp(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), command);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("帮助")
  public void helpQq(QGGroupAtMessageCreateEvent event) {
    log.debug("收到帮助请求 - AuthorId: {}", event.getAuthorId());
    String response =
        helpCommandHandler.handleHelpMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), null);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("帮助 {{command}}")
  public void helpDetailQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("command") String command) {
    log.debug("收到命令详情请求 - AuthorId: {}, Command: {}", event.getAuthorId(), command);
    String response =
        helpCommandHandler.handleHelpMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), command);
    replyHelper.replyQQ(event, response);
  }
}
