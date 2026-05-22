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
import top.stillmisty.xiantao.handle.command.ViewCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewListener {

  private final ViewCommandHandler viewCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("查看 {{target}}")
  public void view(OneBotMessageEvent event, @FilterValue("target") String target) {
    log.debug("[OneBot] 收到查看请求 - AuthorId: {}, Target: {}", event.getAuthorId(), target);
    replyHelper.oneBot(event, target, viewCommandHandler::handleView);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("查看 {{target}}")
  public void viewQq(QGGroupAtMessageCreateEvent event, @FilterValue("target") String target) {
    log.debug("[QQ] 收到查看请求 - AuthorId: {}, Target: {}", event.getAuthorId(), target);
    replyHelper.qq(event, target, viewCommandHandler::handleView);
  }
}
