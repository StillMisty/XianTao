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
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
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
    String response =
        teamCommandHandler.handleTeamStatus(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队邀请 {{targetNickname,[\\S]+}}")
  public void invite(
      OneBotMessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    String response =
        teamCommandHandler.handleInvite(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            targetNickname,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队接受 {{invitationId,[\\d]*}}")
  public void accept(OneBotMessageEvent event, @FilterValue("invitationId") String invitationId) {
    String response =
        teamCommandHandler.handleAccept(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            invitationId,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队拒绝 {{invitationId,[\\d]*}}")
  public void reject(OneBotMessageEvent event, @FilterValue("invitationId") String invitationId) {
    String response =
        teamCommandHandler.handleReject(
            PlatformType.ONE_BOT_V11,
            event.getAuthorId().toString(),
            invitationId,
            TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("组队离开")
  public void leave(OneBotMessageEvent event) {
    String response =
        teamCommandHandler.handleLeave(
            PlatformType.ONE_BOT_V11, event.getAuthorId().toString(), TextFormat.PLAIN);
    replyHelper.replyOneBot(event, response);
  }

  // === QQ ===

  @Listener
  @ContentTrim
  @Filter("组队")
  public void teamStatusQq(QGGroupAtMessageCreateEvent event) {
    String response =
        teamCommandHandler.handleTeamStatus(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队邀请 {{targetNickname,[\\S]+}}")
  public void inviteQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("targetNickname") String targetNickname) {
    String response =
        teamCommandHandler.handleInvite(
            PlatformType.QQ, event.getAuthorId().toString(), targetNickname, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队接受 {{invitationId,[\\d]*}}")
  public void acceptQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("invitationId") String invitationId) {
    String response =
        teamCommandHandler.handleAccept(
            PlatformType.QQ, event.getAuthorId().toString(), invitationId, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter(value = "组队拒绝 {{invitationId,[\\d]*}}")
  public void rejectQq(
      QGGroupAtMessageCreateEvent event, @FilterValue("invitationId") String invitationId) {
    String response =
        teamCommandHandler.handleReject(
            PlatformType.QQ, event.getAuthorId().toString(), invitationId, TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }

  @Listener
  @ContentTrim
  @Filter("组队离开")
  public void leaveQq(QGGroupAtMessageCreateEvent event) {
    String response =
        teamCommandHandler.handleLeave(
            PlatformType.QQ, event.getAuthorId().toString(), TextFormat.MARKDOWN);
    replyHelper.replyQQ(event, response);
  }
}
