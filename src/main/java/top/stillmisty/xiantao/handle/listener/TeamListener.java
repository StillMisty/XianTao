package top.stillmisty.xiantao.handle.listener;

import lombok.RequiredArgsConstructor;
import love.forte.simbot.event.MessageEvent;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.FilterValue;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.handle.command.TeamCommandHandler;
import top.stillmisty.xiantao.handle.interceptor.RequireAuth;

@Component
@RequiredArgsConstructor
public class TeamListener {

  private final TeamCommandHandler teamCommandHandler;
  private final ReplyHelper replyHelper;

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("组队")
  public void teamStatus(MessageEvent event) {
    replyHelper.dispatch(event, "组队", teamCommandHandler::handleTeamStatus);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("组队邀请\\s*{{targetNickname,\\S+}}")
  public void invite(MessageEvent event, @FilterValue("targetNickname") String targetNickname) {
    replyHelper.dispatch(event, "组队邀请", targetNickname, teamCommandHandler::handleInvite);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("组队接受\\s*{{invitationId,\\d+}}")
  public void accept(MessageEvent event, @FilterValue("invitationId") String invitationId) {
    replyHelper.dispatch(event, "组队接受", invitationId, teamCommandHandler::handleAccept);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("组队拒绝\\s*{{invitationId,\\d+}}")
  public void reject(MessageEvent event, @FilterValue("invitationId") String invitationId) {
    replyHelper.dispatch(event, "组队拒绝", invitationId, teamCommandHandler::handleReject);
  }

  @RequireAuth
  @Listener
  @ContentTrim
  @Filter("组队离开")
  public void leave(MessageEvent event) {
    replyHelper.dispatch(event, "组队离开", teamCommandHandler::handleLeave);
  }
}
