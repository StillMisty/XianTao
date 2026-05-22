package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusListener {

  private final CultivationCommandHandler cultivationCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("状态")
  public void status(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到状态查询请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, cultivationCommandHandler::handleStatus);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("状态")
  public void statusQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到状态查询请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, cultivationCommandHandler::handleStatus);
  }
}
