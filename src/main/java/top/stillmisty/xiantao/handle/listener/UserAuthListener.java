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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthListener {

  private final CultivationCommandHandler cultivationCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter(value = "改号 {{newNickname}}")
  public void changeNickname(
      OneBotMessageEvent event, @FilterValue("newNickname") String newNickname) {
    log.info("收到改号请求 - AuthorId: {}, NewNickname: {}", event.getAuthorId(), newNickname);
    String response =
        cultivationCommandHandler.handleChangeNickname(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), newNickname);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "我要修仙 {{nickname}}")
  public void register(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    log.info("收到注册请求 - AuthorId: {}, Nickname: {}", event.getAuthorId(), nickname);
    String response =
        cultivationCommandHandler.handleRegister(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), nickname);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter(value = "改号 {{newNickname}}")
  public void changeNicknameQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("newNickname") String newNickname) {
    log.debug("收到改号请求 - AuthorId: {}, NewNickname: {}", event.getAuthorId(), newNickname);
    String response =
        cultivationCommandHandler.handleChangeNicknameMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), newNickname);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "我要修仙 {{nickname}}")
  public void registerQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    log.info("收到注册请求 - AuthorId: {}, Nickname: {}", event.getAuthorId(), nickname);
    String response =
        cultivationCommandHandler.handleRegisterMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), nickname);
    replyHelper.replyQQ(event, response);
  }
}
