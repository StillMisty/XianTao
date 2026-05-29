package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.HelpCommandHandler;

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
    replyHelper.oneBot(event, "帮助", (p, o, f) -> helpCommandHandler.handleHelp(p, o, null, f));
  }

  @Listener
  @ContentTrim
  @Filter("帮助\\s*{{command}}")
  public void helpDetail(OneBotMessageEvent event, @FilterValue("command") String command) {
    replyHelper.oneBot(event, "命令详情", command, helpCommandHandler::handleHelp);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("帮助")
  public void helpQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "帮助", (p, o, f) -> helpCommandHandler.handleHelp(p, o, null, f));
  }

  @Listener
  @ContentTrim
  @Filter("帮助\\s*{{command}}")
  public void helpDetailQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("command") String command) {
    replyHelper.qq(event, "命令详情", command, helpCommandHandler::handleHelp);
  }
}
