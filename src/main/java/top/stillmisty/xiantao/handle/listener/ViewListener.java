package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import love.forte.simbot.quantcat.common.filter.FilterMode;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.ViewCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class ViewListener {

  private final ViewCommandHandler viewCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter(mode = FilterMode.INTERCEPTOR, value = "查看\\s*{{target}}")
  public void view(MessageEvent event, @FilterValue("target") String target) {
    replyHelper.dispatch(event, "查看", target, viewCommandHandler::handleView);
  }
}
