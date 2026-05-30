package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.HelpCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class HelpListener {

  private final HelpCommandHandler helpCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("帮助")
  public void help(MessageEvent event) {
    replyHelper.dispatch(event, "帮助", f -> helpCommandHandler.handleHelp("", f));
  }

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("帮助\\s*{{command}}")
  public void helpDetail(MessageEvent event, @FilterValue("command") String command) {
    replyHelper.dispatch(event, "命令详情", command, helpCommandHandler::handleHelp);
  }
}
