package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.FudiCommandHandler;

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
    replyHelper.oneBot(event, "福地", fudiCommandHandler::handleFudiStatus);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGrid(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "福地地块", fudiCommandHandler::handleFudiGrid);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpirit(OneBotMessageEvent event, @FilterValue("content") String content) {
    replyHelper.oneBot(event, "地灵对话", content, fudiCommandHandler::handleSpiritChat);
  }

  @Listener
  @ContentTrim
  @Filter("福地渡劫")
  public void handleFudiTribulation(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "福地渡劫", fudiCommandHandler::handleTriggerTribulation);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("福地")
  public void handleFudiQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "福地", fudiCommandHandler::handleFudiStatus);
  }

  @Listener
  @ContentTrim
  @Filter("福地地块")
  public void handleFudiGridQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "福地地块", fudiCommandHandler::handleFudiGrid);
  }

  @Listener
  @ContentTrim
  @Filter("地灵 {{content}}")
  public void handleFudiSpiritQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("content") String content) {
    replyHelper.qq(event, "地灵对话", content, fudiCommandHandler::handleSpiritChat);
  }

  @Listener
  @ContentTrim
  @Filter("福地渡劫")
  public void handleFudiTribulationQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "福地渡劫", fudiCommandHandler::handleTriggerTribulation);
  }
}
