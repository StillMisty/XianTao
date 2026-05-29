package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotMessageEvent;
import love.forte.simbot.component.qguild.event.QGGroupAtMessageCreateEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

@Component
@RequiredArgsConstructor
public class CultivationListener {

  private final CultivationCommandHandler cultivationCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("突破")
  public void breakthrough(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "突破", cultivationCommandHandler::handleBreakthrough);
  }

  @Listener
  @ContentTrim
  @Filter("护道\\s*{{nickname}}")
  public void establishProtection(
      OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.oneBot(event, "护道", nickname, cultivationCommandHandler::handleEstablishProtection);
  }

  @Listener
  @ContentTrim
  @Filter("护道解除\\s*{{nickname}}")
  public void removeProtection(OneBotMessageEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.oneBot(event, "护道解除", nickname, cultivationCommandHandler::handleRemoveProtection);
  }

  @Listener
  @ContentTrim
  @Filter("护道查询")
  public void queryProtection(OneBotMessageEvent event) {
    replyHelper.oneBot(event, "护道查询", cultivationCommandHandler::handleQueryProtection);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("突破")
  public void breakthroughQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "突破", cultivationCommandHandler::handleBreakthrough);
  }

  @Listener
  @ContentTrim
  @Filter("护道\\s*{{nickname}}")
  public void establishProtectionQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.qq(event, "护道", nickname, cultivationCommandHandler::handleEstablishProtection);
  }

  @Listener
  @ContentTrim
  @Filter("护道解除\\s*{{nickname}}")
  public void removeProtectionQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("nickname") String nickname) {
    replyHelper.qq(event, "护道解除", nickname, cultivationCommandHandler::handleRemoveProtection);
  }

  @Listener
  @ContentTrim
  @Filter("护道查询")
  public void queryProtectionQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, "护道查询", cultivationCommandHandler::handleQueryProtection);
  }
}
