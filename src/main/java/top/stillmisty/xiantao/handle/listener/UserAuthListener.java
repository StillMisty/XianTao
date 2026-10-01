package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.UserCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;

@Slf4j
@Component
@RequiredArgsConstructor
@CommandGroup
public class UserAuthListener {

  private final UserCommandHandler userCommandHandler;
  private final ReplyHelper replyHelper;
  private final PlatformRegistry platformRegistry;

  @RequireAuth
  @Command("改号\\s*{{newNickname}}")
  public void changeNickname(QqIncomingMessage event, @Arg("newNickname") String newNickname) {
    replyHelper.dispatch(event, "改号", newNickname, userCommandHandler::handleChangeNickname);
  }

  @Command("我要修仙\\s*{{nickname}}")
  public void register(QqIncomingMessage event, @Arg("nickname") String nickname) {
    var handler = platformRegistry.getHandler(event);
    log.info(
        "[{}] 收到注册请求 - AuthorId: {}, Nickname: {}",
        handler.getPlatformType(),
        handler.extractOpenId(event),
        nickname);
    replyHelper.dispatch(
        event,
        "注册",
        nickname,
        (arg, fmt) ->
            userCommandHandler.handleRegister(
                handler.getPlatformType(), handler.extractOpenId(event), nickname, fmt));
  }
}
