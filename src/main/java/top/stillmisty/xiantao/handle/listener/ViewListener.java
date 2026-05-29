package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.ViewCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class ViewListener {

  private final ViewCommandHandler viewCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("查看\\s*{{target}}")
  public void view(OneBotMessageEvent event, @FilterValue("target") String target) {
    replyHelper.oneBot(event, "查看", target, viewCommandHandler::handleView);
  }

  // === QQ ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("查看\\s*{{target}}")
  public void viewQq(QGGroupAtMessageCreateEvent event, @FilterValue("target") String target) {
    replyHelper.qq(event, "查看", target, viewCommandHandler::handleView);
  }
}
