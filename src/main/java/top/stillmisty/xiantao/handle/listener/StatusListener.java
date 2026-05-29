package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.StatusCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class StatusListener {

  private final StatusCommandHandler statusCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("状态")
  public void status(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "状态查询", statusCommandHandler::handleStatus);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @RequireAuth
  @Filter("状态")
  public void statusQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "状态查询", statusCommandHandler::handleStatus);
  }
}
