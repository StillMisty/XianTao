package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.TravelerCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class TravelerListener {

  private final TravelerCommandHandler travelerCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("游商\\s*{{content}}")
  public void traveler(QqIncomingMessage event, @Arg("content") String content) {
    replyHelper.dispatch(event, "游商", content, travelerCommandHandler::handleTraveler);
  }
}
