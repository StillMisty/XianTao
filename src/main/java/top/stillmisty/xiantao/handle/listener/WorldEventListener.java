package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.WorldEventCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class WorldEventListener {

  private final WorldEventCommandHandler worldEventCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("世界事件")
  public void listEvents(MessageEvent event) {
    replyHelper.dispatch(event, "世界事件列表", worldEventCommandHandler::handleListEvents);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("参与事件\\s*{{eventId,\\d+}}")
  public void joinEvent(MessageEvent event, @FilterValue("eventId") String eventId) {
    replyHelper.dispatch(event, "参与世界事件", eventId, worldEventCommandHandler::handleJoinEvent);
  }
}
