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
import top.stillmisty.xiantao.handle.command.PvpCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class PvpListener {
  private final PvpCommandHandler pvpCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("切磋 {{targetNickname}}")
  public void spar(OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    log.debug("收到切磋请求 - AuthorId: {}, Target: {}", event.getAuthorId(), targetNickname);
    String response =
        pvpCommandHandler.handleSpar(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), targetNickname);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("切磋 {{targetNickname}}")
  public void sparQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    log.debug("收到切磋请求 - AuthorId: {}, Target: {}", event.getAuthorId(), targetNickname);
    String response =
        pvpCommandHandler.handleSparMarkdown(
            PlatformType.QQ, event.getAuthorId().toString(), targetNickname);
    replyHelper.replyQQ(event, response);
  }
}
