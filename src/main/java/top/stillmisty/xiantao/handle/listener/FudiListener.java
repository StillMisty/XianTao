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
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class FudiListener {

  private final FudiCommandHandler fudiCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudi(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到福地请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, fudiCommandHandler::handleFudiStatus);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGrid(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, fudiCommandHandler::handleFudiGrid);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpirit(OneBotMessageEvent event, @FilterValue("content") String content) {
    log.debug("[OneBot] 收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    replyHelper.oneBot(event, content, fudiCommandHandler::handleSpiritChat);
  }

  @Listener
  @ContentTrim
  @Filter("福地渡劫")
  public void handleFudiTribulation(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到福地渡劫请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.oneBot(event, fudiCommandHandler::handleTriggerTribulation);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudiQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到福地请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, fudiCommandHandler::handleFudiStatus);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGridQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, fudiCommandHandler::handleFudiGrid);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpiritQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    log.debug("[QQ] 收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    replyHelper.qq(event, content, fudiCommandHandler::handleSpiritChat);
  }

  @Listener
  @ContentTrim
  @Filter("福地渡劫")
  public void handleFudiTribulationQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到福地渡劫请求 - AuthorId: {}", event.getAuthorId());
    replyHelper.qq(event, fudiCommandHandler::handleTriggerTribulation);
  }
}
