package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.FortuneCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class FortuneListener {

  private final FortuneCommandHandler fortuneCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("今日运势")
  public void fortune(QqIncomingMessage event) {
    replyHelper.dispatch(event, "今日运势", fortuneCommandHandler::handleFortune);
  }
}
