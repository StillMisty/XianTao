package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.qqgateway.QqIncomingMessage;
import top.stillmisty.xiantao.handle.command.WorldEventCommandHandler;
import top.stillmisty.xiantao.handle.dispatch.Arg;
import top.stillmisty.xiantao.handle.dispatch.Command;
import top.stillmisty.xiantao.handle.dispatch.CommandGroup;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
@CommandGroup
public class WorldEventListener {

  private final WorldEventCommandHandler worldEventCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Command("世界事件")
  public void listEvents(QqIncomingMessage event) {
    replyHelper.dispatch(event, "世界事件列表", worldEventCommandHandler::handleListEvents);
  }

  @RequireAuth
  @Command("参与事件\\s*{{eventId,\\d+}}")
  public void joinEvent(QqIncomingMessage event, @Arg("eventId") String eventId) {
    replyHelper.dispatch(event, "参与世界事件", eventId, worldEventCommandHandler::handleJoinEvent);
  }
}
