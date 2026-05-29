package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.pvp.TeamService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamCommandHandler implements CommandGroup {

  private final TeamService teamService;

  public String handleTeamStatus(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> teamService.getTeamStatus(userId), fmt, text -> text, msg -> "操作失败: " + msg);
  }

  public String handleInvite(String targetNickname, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> teamService.invitePlayer(userId, targetNickname),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleAccept(String invitationId, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> teamService.acceptInvitation(userId, invitationId),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleReject(String invitationId, TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> teamService.rejectInvitation(userId, invitationId),
        fmt,
        text -> text,
        msg -> "操作失败: " + msg);
  }

  public String handleLeave(TextFormat fmt) {
    Long userId = UserContext.requireCurrentUserId();
    return CommandHandlerHelper.safeCall(
        () -> teamService.leaveTeam(userId), fmt, text -> text, msg -> "操作失败: " + msg);
  }

  @Override
  public String groupName() {
    return "组队";
  }

  @Override
  public String groupSummary() {
    return "组队协作、邀请管理";
  }

  @Override
  public String groupDescription() {
    return "组队邀请、管理队伍成员";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("组队", "查看队伍状态和待处理邀请", "组队"),
        new CommandEntry("组队邀请 「道号」", "邀请玩家组队", "组队邀请 张三"),
        new CommandEntry("组队接受 [邀请编号]", "接受组队邀请（不填编号默认接受最新）", "组队接受 42"),
        new CommandEntry("组队拒绝 [邀请编号]", "拒绝组队邀请（不填编号拒绝全部）", "组队拒绝 42"),
        new CommandEntry("组队离开", "离开当前队伍", "组队离开"));
  }
}
