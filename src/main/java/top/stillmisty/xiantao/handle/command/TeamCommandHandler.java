package top.stillmisty.xiantao.handle.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.TeamService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamCommandHandler implements CommandGroup {

  private final TeamService teamService;

  public String handleTeamStatus(PlatformType platform, String openId, TextFormat fmt) {
    var result = teamService.getTeamStatus(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleInvite(
      PlatformType platform, String openId, String targetNickname, TextFormat fmt) {
    var result = teamService.invitePlayer(platform, openId, targetNickname);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleAccept(
      PlatformType platform, String openId, String invitationId, TextFormat fmt) {
    var result = teamService.acceptInvitation(platform, openId, invitationId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleReject(
      PlatformType platform, String openId, String invitationId, TextFormat fmt) {
    var result = teamService.rejectInvitation(platform, openId, invitationId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  public String handleLeave(PlatformType platform, String openId, TextFormat fmt) {
    var result = teamService.leaveTeam(platform, openId);
    return switch (result) {
      case ServiceResult.Success(var text) -> text;
      case ServiceResult.Failure(var code, var msg) -> "操作失败: " + msg;
    };
  }

  @Override
  public String groupName() {
    return "组队";
  }

  @Override
  public String groupDescription() {
    return "组队邀请、管理队伍成员";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("组队", "查看队伍状态和待处理邀请", "组队"),
        new CommandEntry("组队邀请 {{道号}}", "邀请玩家组队", "组队邀请 张三"),
        new CommandEntry("组队接受 [邀请编号]", "接受组队邀请（不填编号默认接受最新）", "组队接受 42"),
        new CommandEntry("组队拒绝 [邀请编号]", "拒绝组队邀请（不填编号拒绝全部）", "组队拒绝 42"),
        new CommandEntry("组队离开", "离开当前队伍", "组队离开"));
  }
}
