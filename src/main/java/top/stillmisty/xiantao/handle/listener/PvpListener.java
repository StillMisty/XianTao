package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.PvpCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class PvpListener {
  private final PvpCommandHandler pvpCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("切磋\\s*{{targetNickname}}")
  public void spar(QqIncomingMessage event, @Arg("targetNickname") String targetNickname) {
    replyHelper.dispatch(event, "切磋", targetNickname, pvpCommandHandler::handleSpar);
  }
}
