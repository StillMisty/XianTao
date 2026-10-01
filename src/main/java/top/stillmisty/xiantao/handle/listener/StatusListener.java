package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.StatusCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class StatusListener {

  private final StatusCommandHandler statusCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("状态")
  public void status(QqIncomingMessage event) {
    replyHelper.dispatch(event, "状态查询", statusCommandHandler::handleStatus);
  }
}
