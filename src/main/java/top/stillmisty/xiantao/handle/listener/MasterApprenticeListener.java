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
import top.stillmisty.xiantao.handle.command.MasterApprenticeCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class MasterApprenticeListener {

  private final MasterApprenticeCommandHandler masterApprenticeCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("拜师 {{targetNickname,[\\S]+}}")
  public void requestMentor(
      OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.oneBot(event, targetNickname, masterApprenticeCommandHandler::handleRequestMentor);
  }

  @Listener
  @ContentTrim
  @Filter("收徒 {{targetNickname,[\\S]+}}")
  public void requestApprentice(
      OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.oneBot(
        event, targetNickname, masterApprenticeCommandHandler::handleRequestApprentice);
  }

  @Listener
  @ContentTrim
  @Filter("师徒")
  public void status(OneBotMessageEvent event) {
    replyHelper.oneBot(event, masterApprenticeCommandHandler::handleStatus);
  }

  @Listener
  @ContentTrim
  @Filter("逐出师门 {{targetNickname,[\\S]+}}")
  public void dismiss(
      OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.oneBot(event, targetNickname, masterApprenticeCommandHandler::handleDismiss);
  }

  @Listener
  @ContentTrim
  @Filter("叛师")
  public void renounce(OneBotMessageEvent event) {
    replyHelper.oneBot(event, masterApprenticeCommandHandler::handleRenounce);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("拜师 {{targetNickname,[\\S]+}}")
  public void requestMentorQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.qq(event, targetNickname, masterApprenticeCommandHandler::handleRequestMentor);
  }

  @Listener
  @ContentTrim
  @Filter("收徒 {{targetNickname,[\\S]+}}")
  public void requestApprenticeQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.qq(event, targetNickname, masterApprenticeCommandHandler::handleRequestApprentice);
  }

  @Listener
  @ContentTrim
  @Filter("师徒")
  public void statusQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, masterApprenticeCommandHandler::handleStatus);
  }

  @Listener
  @ContentTrim
  @Filter("逐出师门 {{targetNickname,[\\S]+}}")
  public void dismissQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.qq(event, targetNickname, masterApprenticeCommandHandler::handleDismiss);
  }

  @Listener
  @ContentTrim
  @Filter("叛师")
  public void renounceQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, masterApprenticeCommandHandler::handleRenounce);
  }
}
