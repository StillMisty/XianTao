package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.PvpCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class PvpListener {
  private final PvpCommandHandler pvpCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("切磋\\s*{{targetNickname}}")
  public void spar(OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.oneBot(event, "切磋", targetNickname, pvpCommandHandler::handleSpar);
  }

  // === QQ ===

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("切磋\\s*{{targetNickname}}")
  public void sparQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.qq(event, "切磋", targetNickname, pvpCommandHandler::handleSpar);
  }
}
