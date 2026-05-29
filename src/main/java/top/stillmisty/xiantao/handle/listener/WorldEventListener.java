package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.WorldEventCommandHandler;

@Component
@RequiredArgsConstructor
public class WorldEventListener {

  private final WorldEventCommandHandler worldEventCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("世界事件")
  public void listEvents(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "世界事件列表", worldEventCommandHandler::handleListEvents);
  }

  @Listener
  @ContentTrim
  @Filter("参与事件\\s*{{eventId,\\d+}}")
  public void joinEvent(OneBotMessageEvent event, @FilterValue("eventId") String eventId) {
    replyHelper.oneBot(event, "参与世界事件", eventId, worldEventCommandHandler::handleJoinEvent);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("世界事件")
  public void listEventsQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "世界事件列表", worldEventCommandHandler::handleListEvents);
  }

  @Listener
  @ContentTrim
  @Filter("参与事件\\s*{{eventId,\\d+}}")
  public void joinEventQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("eventId") String eventId) {
    replyHelper.qq(event, "参与世界事件", eventId, worldEventCommandHandler::handleJoinEvent);
  }
}
