package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class CultivationListener {

  private final CultivationCommandHandler cultivationCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("突破")
  public void breakthrough(MessageEvent event) {
    replyHelper.dispatch(event, "突破", cultivationCommandHandler::handleBreakthrough);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("护道\\s*{{nickname}}")
  public void establishProtection(MessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.dispatch(
        event, "护道", nickname, cultivationCommandHandler::handleEstablishProtection);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("护道解除\\s*{{nickname}}")
  public void removeProtection(MessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.dispatch(
        event, "护道解除", nickname, cultivationCommandHandler::handleRemoveProtection);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("护道查询")
  public void queryProtection(MessageEvent event) {
    replyHelper.dispatch(event, "护道查询", cultivationCommandHandler::handleQueryProtection);
  }
}
