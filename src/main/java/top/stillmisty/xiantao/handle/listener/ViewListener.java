package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.ViewCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class ViewListener {

  private final ViewCommandHandler viewCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("查看\\s*{{target}}")
  public void view(QqIncomingMessage event, @Arg("target") String target) {
    replyHelper.dispatch(event, "查看", target, viewCommandHandler::handleView);
  }
}
