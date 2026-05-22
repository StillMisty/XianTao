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
import top.stillmisty.xiantao.handle.command.TeamCommandHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamListener {

  private final TeamCommandHandler teamCommandHandler;
  private final ReplyHelper replyHelper;

  // === OneBotV11 ===

  @Listener
  @ContentTrim
  @Filter("组队")
  public void teamStatus(OneBotMessageEvent event) {
    replyHelper.oneBot(event, teamCommandHandler::handleTeamStatus);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队邀请 {{targetNickname,[\\S]+}}")
  public void invite(
      OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.oneBot(event, targetNickname, teamCommandHandler::handleInvite);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队接受 {{invitationId,[\\d]*}}")
  public void accept(OneBotMessageEvent event, @FilterValue("invitationId") String invitationId) {
    replyHelper.oneBot(event, invitationId, teamCommandHandler::handleAccept);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队拒绝 {{invitationId,[\\d]*}}")
  public void reject(OneBotMessageEvent event, @FilterValue("invitationId") String invitationId) {
    replyHelper.oneBot(event, invitationId, teamCommandHandler::handleReject);
  }

  @Listener
  @ContentTrim
  @Filter("组队离开")
  public void leave(OneBotMessageEvent event) {
    replyHelper.oneBot(event, teamCommandHandler::handleLeave);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("组队")
  public void teamStatusQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, teamCommandHandler::handleTeamStatus);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队邀请 {{targetNickname,[\\S]+}}")
  public void inviteQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.qq(event, targetNickname, teamCommandHandler::handleInvite);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队接受 {{invitationId,[\\d]*}}")
  public void acceptQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("invitationId") String invitationId) {
    replyHelper.qq(event, invitationId, teamCommandHandler::handleAccept);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队拒绝 {{invitationId,[\\d]*}}")
  public void rejectQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("invitationId") String invitationId) {
    replyHelper.qq(event, invitationId, teamCommandHandler::handleReject);
  }

  @Listener
  @ContentTrim
  @Filter("组队离开")
  public void leaveQq(QGGroupAtMessageCreateEvent event) {
    replyHelper.qq(event, teamCommandHandler::handleLeave);
  }
}
