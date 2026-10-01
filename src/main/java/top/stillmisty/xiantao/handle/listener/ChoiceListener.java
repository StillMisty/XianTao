package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.ChoiceCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class ChoiceListener {

  private final ChoiceCommandHandler choiceCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("选\\s*{{choice}}")
  public void choice(QqIncomingMessage event, @Arg("choice") String choice) {
    replyHelper.dispatch(event, "选择", choice, choiceCommandHandler::handleChoice);
  }
}
