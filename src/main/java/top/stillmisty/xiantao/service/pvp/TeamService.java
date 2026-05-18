package top.stillmisty.xiantao.service.pvp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.team.entity.Team;
import top.stillmisty.xiantao.domain.team.entity.TeamInvitation;
import top.stillmisty.xiantao.domain.team.entity.TeamMember;
import top.stillmisty.xiantao.domain.team.enums.InvitationStatus;
import top.stillmisty.xiantao.domain.team.enums.TeamStatus;
import top.stillmisty.xiantao.domain.team.repository.TeamInvitationRepository;
import top.stillmisty.xiantao.domain.team.repository.TeamMemberRepository;
import top.stillmisty.xiantao.domain.team.repository.TeamRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final TeamInvitationRepository invitationRepository;
  private final UserStateService userStateService;

  // ===================== 公开 API =====================

  @Authenticated
  @Transactional
  public ServiceResult<String> getTeamStatus(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(getTeamStatus(userId));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> invitePlayer(
      PlatformType platform, String openId, String targetNickname) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(invitePlayer(userId, targetNickname));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> acceptInvitation(
      PlatformType platform, String openId, String invitationIdStr) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(acceptInvitation(userId, invitationIdStr));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> rejectInvitation(
      PlatformType platform, String openId, String invitationIdStr) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(rejectInvitation(userId, invitationIdStr));
  }

  @Authenticated
  @Transactional
  public ServiceResult<String> leaveTeam(PlatformType platform, String openId) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(leaveTeam(userId));
  }

  // ===================== 内部 API =====================

  public String getTeamStatus(Long userId) {
    User user = userStateService.loadUser(userId);
    StringBuilder sb = new StringBuilder();

    Optional<TeamMember> member = teamMemberRepository.findByUserId(userId);
    if (member.isPresent()) {
      Team team = teamRepository.findById(member.get().getTeamId()).orElseThrow();
      List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
      User leader = userStateService.loadUser(team.getLeaderId());

      sb.append("=== 队伍信息 ===\n");
      sb.append("队长: ").append(leader.getNickname());
      if (team.getLeaderId().equals(userId)) sb.append("（你）");
      sb.append("\n");
      sb.append("人数: ").append(members.size()).append("\n");
      sb.append("成员:\n");
      for (TeamMember m : members) {
        User memberUser = userStateService.loadUser(m.getUserId());
        sb.append("  · ").append(memberUser.getNickname());
        if (m.getUserId().equals(userId)) sb.append("（你）");
        sb.append("\n");
      }
    } else {
      sb.append("你当前不在任何队伍中。\n");
    }

    List<TeamInvitation> pendingInvitations = invitationRepository.findPendingByInviteeId(userId);
    if (!pendingInvitations.isEmpty()) {
      sb.append("\n=== 待处理的组队邀请 ===\n");
      for (TeamInvitation inv : pendingInvitations) {
        User inviter = userStateService.loadUser(inv.getInviterId());
        sb.append("  [#")
            .append(inv.getId())
            .append("] ")
            .append(inviter.getNickname())
            .append(" 邀请你组队\n");
      }
    }

    return sb.toString();
  }

  public String invitePlayer(Long userId, String targetNickname) {
    User inviter = userStateService.loadUserForUpdate(userId);
    User invitee = userStateService.loadUserByNickname(targetNickname);

    if (invitee == null) {
      throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, targetNickname);
    }
    if (invitee.getId().equals(userId)) {
      throw new BusinessException(ErrorCode.TEAM_CANNOT_INVITE_SELF);
    }

    Optional<TeamMember> inviteeMember = teamMemberRepository.findByUserId(invitee.getId());
    if (inviteeMember.isPresent()) {
      throw new BusinessException(ErrorCode.TEAM_INVITEE_ALREADY_IN_TEAM, invitee.getNickname());
    }

    Team team = ensureTeam(userId);

    List<TeamInvitation> existingPending = invitationRepository.findPendingByTeamId(team.getId());
    for (TeamInvitation inv : existingPending) {
      if (inv.getInviteeId().equals(invitee.getId())) {
        return "已向【" + targetNickname + "】发送过组队邀请，请等待对方回应。";
      }
    }

    TeamInvitation invitation = new TeamInvitation();
    invitation.setTeamId(team.getId());
    invitation.setInviterId(userId);
    invitation.setInviteeId(invitee.getId());
    invitation.setStatus(InvitationStatus.PENDING);
    invitation.setCreatedAt(LocalDateTime.now());
    invitation.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    invitationRepository.save(invitation);

    log.info("玩家 {} 邀请 {} 组队, 邀请ID={}", userId, invitee.getId(), invitation.getId());
    return "已向【" + targetNickname + "】发出组队邀请（编号 #" + invitation.getId() + "，5分钟内有效）。";
  }

  public String acceptInvitation(Long userId, String invitationIdStr) {
    User user = userStateService.loadUserForUpdate(userId);

    Optional<TeamMember> existingMember = teamMemberRepository.findByUserId(userId);
    if (existingMember.isPresent()) {
      throw new BusinessException(ErrorCode.TEAM_ALREADY_IN, existingMember.get().getTeamId());
    }

    Long invitationId;
    if (invitationIdStr == null || invitationIdStr.isBlank()) {
      List<TeamInvitation> pending = invitationRepository.findPendingByInviteeId(userId);
      if (pending.isEmpty()) {
        throw new BusinessException(ErrorCode.TEAM_NO_PENDING_INVITATION);
      }
      invitationId = pending.getFirst().getId();
    } else {
      try {
        invitationId = Long.parseLong(invitationIdStr);
      } catch (NumberFormatException e) {
        throw new BusinessException(ErrorCode.TEAM_INVITATION_NOT_FOUND, invitationIdStr);
      }
    }

    TeamInvitation invitation =
        invitationRepository
            .findById(invitationId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TEAM_INVITATION_NOT_FOUND, invitationId));
    if (!invitation.getInviteeId().equals(userId)) {
      throw new BusinessException(ErrorCode.TEAM_INVITATION_NOT_FOR_YOU);
    }
    if (invitation.isPending() || invitation.isExpired()) {
      throw new BusinessException(ErrorCode.TEAM_INVITATION_EXPIRED);
    }

    Team team = teamRepository.findById(invitation.getTeamId()).orElseThrow();
    if (!team.isActive()) {
      throw new BusinessException(ErrorCode.TEAM_INVITATION_EXPIRED);
    }

    TeamMember member = new TeamMember();
    member.setTeamId(team.getId());
    member.setUserId(userId);
    member.setJoinedAt(LocalDateTime.now());
    teamMemberRepository.save(member);

    team.incrementMemberCount();
    teamRepository.save(team);

    invitation.accept();
    invitationRepository.save(invitation);

    List<TeamInvitation> otherPending = invitationRepository.findPendingByInviteeId(userId);
    for (TeamInvitation inv : otherPending) {
      inv.reject();
      invitationRepository.save(inv);
    }

    log.info("玩家 {} 接受邀请 #{} 加入队伍 {}", userId, invitationId, team.getId());
    return "你已加入队伍！当前队伍人数: " + team.getMemberCount() + "人。";
  }

  public String rejectInvitation(Long userId, String invitationIdStr) {
    if (invitationIdStr == null || invitationIdStr.isBlank()) {
      List<TeamInvitation> pending = invitationRepository.findPendingByInviteeId(userId);
      if (pending.isEmpty()) {
        throw new BusinessException(ErrorCode.TEAM_NO_PENDING_INVITATION);
      }
      for (TeamInvitation inv : pending) {
        inv.reject();
        invitationRepository.save(inv);
      }
      return "已拒绝全部 " + pending.size() + " 条组队邀请。";
    }

    Long invitationId;
    try {
      invitationId = Long.parseLong(invitationIdStr);
    } catch (NumberFormatException e) {
      throw new BusinessException(ErrorCode.TEAM_INVITATION_NOT_FOUND, invitationIdStr);
    }

    TeamInvitation invitation =
        invitationRepository
            .findById(invitationId)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.TEAM_INVITATION_NOT_FOUND, invitationId));
    if (!invitation.getInviteeId().equals(userId)) {
      throw new BusinessException(ErrorCode.TEAM_INVITATION_NOT_FOR_YOU);
    }
    if (invitation.isPending()) {
      return "该邀请已处理。";
    }

    invitation.reject();
    invitationRepository.save(invitation);
    return "已拒绝组队邀请 #" + invitationId + "。";
  }

  public String leaveTeam(Long userId) {
    User user = userStateService.loadUserForUpdate(userId);

    Optional<TeamMember> memberOpt = teamMemberRepository.findByUserId(userId);
    if (memberOpt.isEmpty()) {
      throw new BusinessException(ErrorCode.TEAM_NOT_IN);
    }

    TeamMember member = memberOpt.get();
    Team team = teamRepository.findById(member.getTeamId()).orElseThrow();

    if (team.getLeaderId().equals(userId)) {
      teamMemberRepository.deleteByTeamId(team.getId());
      team.setMemberCount(0);
      team.disband();
      teamRepository.save(team);
      log.info("队长 {} 离开队伍 {}，队伍解散", userId, team.getId());
      return "你已离开队伍。队伍已解散。";
    }

    teamMemberRepository.deleteById(member.getId());
    team.decrementMemberCount();
    teamRepository.save(team);

    log.info("玩家 {} 离开队伍 {}", userId, team.getId());
    return "你已离开队伍。";
  }

  // ===================== 私有方法 =====================

  private Team ensureTeam(Long userId) {
    Optional<Team> existing =
        teamRepository.findByLeaderIdAndStatus(userId, TeamStatus.ACTIVE.getCode());
    if (existing.isPresent()) {
      return existing.get();
    }

    Optional<TeamMember> member = teamMemberRepository.findByUserId(userId);
    if (member.isPresent()) {
      Team team = teamRepository.findById(member.get().getTeamId()).orElseThrow();
      if (team.isActive()) {
        throw new BusinessException(ErrorCode.TEAM_ALREADY_IN, team.getId());
      }
    }

    Team team = new Team();
    team.setLeaderId(userId);
    team.setMemberCount(1);
    team.setStatus(TeamStatus.ACTIVE);
    team.setCreatedAt(LocalDateTime.now());
    teamRepository.save(team);

    TeamMember selfMember = new TeamMember();
    selfMember.setTeamId(team.getId());
    selfMember.setUserId(userId);
    selfMember.setJoinedAt(LocalDateTime.now());
    teamMemberRepository.save(selfMember);

    return team;
  }
}
