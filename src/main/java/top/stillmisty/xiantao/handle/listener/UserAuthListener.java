package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.UserCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthListener {

  private final UserCommandHandler userCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter(value = "改号 {{newNickname}}")
  public void changeNickname(
      OneBotMessageEvent event, @FilterValue("newNickname") String newNickname) {
    log.info("[OneBot] 收到改号请求 - AuthorId: {}, NewNickname: {}", event.getAuthorId(), newNickname);
    replyHelper.oneBot(event, newNickname, userCommandHandler::handleChangeNickname);
  }

  @Listener
  @ContentTrim
  @Filter(value = "我要修仙 {{nickname}}")
  public void register(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.info("[OneBot] 收到注册请求 - AuthorId: {}, Nickname: {}", event.getAuthorId(), nickname);
    replyHelper.oneBot(event, nickname, userCommandHandler::handleRegister);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter(value = "改号 {{newNickname}}")
  public void changeNicknameQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("newNickname") String newNickname) {
    log.debug("[QQ] 收到改号请求 - AuthorId: {}, NewNickname: {}", event.getAuthorId(), newNickname);
    replyHelper.qq(event, newNickname, userCommandHandler::handleChangeNickname);
  }

  @Listener
  @ContentTrim
  @Filter(value = "我要修仙 {{nickname}}")
  public void registerQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.info("[QQ] 收到注册请求 - AuthorId: {}, Nickname: {}", event.getAuthorId(), nickname);
    replyHelper.qq(event, nickname, userCommandHandler::handleRegister);
  }
}
