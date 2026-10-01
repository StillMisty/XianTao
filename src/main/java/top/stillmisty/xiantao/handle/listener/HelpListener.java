package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.HelpCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class HelpListener {

  private final HelpCommandHandler helpCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("帮助")
  public void help(QqIncomingMessage event) {
    replyHelper.dispatch(event, "帮助", f -> helpCommandHandler.handleHelp("", f));
  }

  @RequireAuth
  @Command("帮助\\s*{{command}}")
  public void helpDetail(QqIncomingMessage event, @Arg("command") String command) {
    replyHelper.dispatch(event, "命令详情", command, helpCommandHandler::handleHelp);
  }
}
