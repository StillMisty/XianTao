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

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthListener {

  private final UserCommandHandler userCommandHandler;
  private final ReplyHelper replyHelper;

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter(mode = FilterMode.INTERCEPTOR, value = "改号\\s*{{newNickname}}")
  public void changeNickname(MessageEvent event, @FilterValue("newNickname") String newNickname) {
    replyHelper.dispatch(event, "改号", newNickname, userCommandHandler::handleChangeNickname);
  }

  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "我要修仙\\s*{{nickname}}")
  public void register(MessageEvent event, @FilterValue("nickname") String nickname) {
    var platform = ReplyHelper.platformTypeOf(event);
    log.info("[{}] 收到注册请求 - AuthorId: {}, Nickname: {}", platform, event.getAuthorId(), nickname);
    replyHelper.dispatch(
        event,
        "注册",
        nickname,
        (arg, fmt) ->
            userCommandHandler.handleRegister(
                platform, event.getAuthorId().toString(), nickname, fmt));
  }
}
