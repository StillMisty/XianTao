package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class CultivationListener {

  private final CultivationCommandHandler cultivationCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("突破")
  public void breakthrough(QqIncomingMessage event) {
    replyHelper.dispatch(event, "突破", cultivationCommandHandler::handleBreakthrough);
  }

  @RequireAuth
  @Command("护道(?!解除|查询)\\s*{{nickname,\\S+}}")
  public void establishProtection(QqIncomingMessage event, @Arg("nickname") String nickname) {
    replyHelper.dispatch(
        event, "护道", nickname, cultivationCommandHandler::handleEstablishProtection);
  }

  @RequireAuth
  @Command("护道解除\\s*{{nickname,\\S+}}")
  public void removeProtection(QqIncomingMessage event, @Arg("nickname") String nickname) {
    replyHelper.dispatch(
        event, "护道解除", nickname, cultivationCommandHandler::handleRemoveProtection);
  }

  @RequireAuth
  @Command("护道查询")
  public void queryProtection(QqIncomingMessage event) {
    replyHelper.dispatch(event, "护道查询", cultivationCommandHandler::handleQueryProtection);
  }
}
