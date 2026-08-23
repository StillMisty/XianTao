package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.UserCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;
import top.stillmisty.xiantao.handle.platform.PlatformRegistry;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthListener {

  private final UserCommandHandler userCommandHandler;
  private final ReplyHelper replyHelper;
  private final PlatformRegistry platformRegistry;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "改号\\s*{{newNickname}}")
  public void changeNickname(MessageEvent event, @FilterValue("newNickname") String newNickname) {
    replyHelper.dispatch(event, "改号", newNickname, userCommandHandler::handleChangeNickname);
  }

  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, priority = 50, value = "我要修仙\\s*{{nickname}}")
  public void register(MessageEvent event, @FilterValue("nickname") String nickname) {
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
