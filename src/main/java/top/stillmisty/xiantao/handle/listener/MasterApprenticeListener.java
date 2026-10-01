package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.MasterApprenticeCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class MasterApprenticeListener {

  private final MasterApprenticeCommandHandler masterApprenticeCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("拜师\\s*{{targetNickname,\\S+}}")
  public void requestMentor(QqIncomingMessage event, @Arg("targetNickname") String targetNickname) {
    replyHelper.dispatch(
        event, "拜师", targetNickname, masterApprenticeCommandHandler::handleRequestMentor);
  }

  @RequireAuth
  @Command("收徒\\s*{{targetNickname,\\S+}}")
  public void requestApprentice(
      QqIncomingMessage event, @Arg("targetNickname") String targetNickname) {
    replyHelper.dispatch(
        event, "收徒", targetNickname, masterApprenticeCommandHandler::handleRequestApprentice);
  }

  @RequireAuth
  @Command("师徒")
  public void status(QqIncomingMessage event) {
    replyHelper.dispatch(event, "师徒", masterApprenticeCommandHandler::handleStatus);
  }

  @RequireAuth
  @Command("逐出师门\\s*{{targetNickname,\\S+}}")
  public void dismiss(QqIncomingMessage event, @Arg("targetNickname") String targetNickname) {
    replyHelper.dispatch(
        event, "逐出师门", targetNickname, masterApprenticeCommandHandler::handleDismiss);
  }

  @RequireAuth
  @Command("叛师")
  public void renounce(QqIncomingMessage event) {
    replyHelper.dispatch(event, "叛师", masterApprenticeCommandHandler::handleRenounce);
  }
}
