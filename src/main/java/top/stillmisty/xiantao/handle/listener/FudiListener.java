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
import top.stillmisty.xiantao.handle.TextFormat;
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
    String response =
        fudiCommandHandler.handleFudiStatus(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGrid(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiGrid(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpirit(OneBotMessageEvent event, @FilterValue("content") String content) {
    log.debug("[OneBot] 收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    String response =
        fudiCommandHandler.handleSpiritChat(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), content, TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地渡劫")
  public void handleFudiTribulation(OneBotMessageEvent event) {
    log.debug("[OneBot] 收到福地渡劫请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleTriggerTribulation(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudiQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到福地请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiStatus(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGridQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到福地地块请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleFudiGrid(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpiritQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    log.debug("[QQ] 收到地灵自然语言请求 - AuthorId: {}, Content: {}", event.getAuthorId(), content);
    String response =
        fudiCommandHandler.handleSpiritChat(
            PlatformType.QQ, event.getAuthorId().toString(), content, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("福地渡劫")
  public void handleFudiTribulationQq(QGGroupAtMessageCreateEvent event) {
    log.debug("[QQ] 收到福地渡劫请求 - AuthorId: {}", event.getAuthorId());
    String response =
        fudiCommandHandler.handleTriggerTribulation(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }
}
