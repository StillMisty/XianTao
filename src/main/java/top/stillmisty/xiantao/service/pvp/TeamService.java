package top.stillmisty.xiantao.service.pvp;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.team.entity.Team;
import top.stillmisty.xiantao.domain.team.entity.TeamInvitation;
import top.stillmisty.xiantao.domain.team.entity.TeamMember;
import top.stillmisty.xiantao.domain.team.enums.InvitationStatus;
import top.stillmisty.xiantao.domain.team.enums.TeamStatus;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.TeamInvitationRepository;
import top.stillmisty.xiantao.infrastructure.repository.TeamMemberRepository;
import top.stillmisty.xiantao.infrastructure.repository.TeamRepository;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.UserStateService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final TeamInvitationRepository invitationRepository;
  private final UserStateService userStateService;
  private final UserRepository userRepository;

  // ===================== 公开 API =====================

  @Transactional
  public ServiceResult<String> getTeamStatus(Long userId) {
    return new ServiceResult.Success<>(getTeamStatusInternal(userId));
  }

  @Transactional
  public ServiceResult<String> invitePlayer(Long userId, String targetNickname) {
    return new ServiceResult.Success<>(invitePlayerInternal(userId, targetNickname));
  }

  @Transactional
  public ServiceResult<String> acceptInvitation(Long userId, String invitationIdStr) {
    return new ServiceResult.Success<>(acceptInvitationInternal(userId, invitationIdStr));
  }

  @Transactional
  public ServiceResult<String> rejectInvitation(Long userId, String invitationIdStr) {
    return new ServiceResult.Success<>(rejectInvitationInternal(userId, invitationIdStr));
  }

  @Transactional
  public ServiceResult<String> leaveTeam(Long userId) {
    return new ServiceResult.Success<>(leaveTeamInternal(userId));
  }

  // ===================== 内部 API =====================

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "team_status", key = "#userId")
  public String getTeamStatusInternal(Long userId) {
    userStateService.loadUserReadOnly(userId);
    StringBuilder sb = new StringBuilder();

    Optional<TeamMember> member = teamMemberRepository.findByUserId(userId);
    if (member.isPresent()) {
      Team team =
          teamRepository
              .findById(member.get().getTeamId())
              .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_IN));
      List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());

      // 批量查询所有成员用户
      List<Long> memberIds = members.stream().map(TeamMember::getUserId).toList();
      Map<Long, User> memberUserMap =
          userRepository.findByIds(memberIds).stream()
              .collect(Collectors.toMap(User::getId, u -> u));

      User leader = memberUserMap.get(team.getLeaderId());
      if (leader == null) {
        leader = userStateService.loadUserReadOnly(team.getLeaderId());
      }

      sb.append("=== 队伍信息 ===\n");
      sb.append("队长: ").append(leader.getNickname());
      if (team.getLeaderId().equals(userId)) sb.append("（你）");
      sb.append("\n");
      sb.append("人数: ").append(members.size()).append("\n");
      sb.append("成员:\n");
      for (TeamMember m : members) {
        User memberUser = memberUserMap.get(m.getUserId());
        if (memberUser == null) {
          memberUser = userStateService.loadUserReadOnly(m.getUserId());
        }
        sb.append("  · ").append(memberUser.getNickname());
        if (m.getUserId().equals(userId)) sb.append("（你）");
        sb.append("\n");
      }
    } else {
      sb.append("你当前不在任何队伍中。\n");
    }

    List<TeamInvitation> pendingInvitations = invitationRepository.findPendingByInviteeId(userId);
    if (!pendingInvitations.isEmpty()) {
      // 批量查询邀请者
      List<Long> inviterIds =
          pendingInvitations.stream().map(TeamInvitation::getInviterId).toList();
      Map<Long, User> inviterMap =
          userRepository.findByIds(inviterIds).stream()
              .collect(Collectors.toMap(User::getId, u -> u));

      sb.append("\n=== 待处理的组队邀请 ===\n");
      for (TeamInvitation inv : pendingInvitations) {
        User inviter = inviterMap.get(inv.getInviterId());
        if (inviter == null) {
          inviter = userStateService.loadUserReadOnly(inv.getInviterId());
        }
        sb.append("  [#")
            .append(inv.getId())
            .append("] ")
            .append(inviter.getNickname())
            .append(" 邀请你组队\n");
      }
    }

    return sb.toString();
  }

  public String invitePlayerInternal(Long userId, String targetNickname) {
    userStateService.loadUser(userId);
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
        return ("已向【" + targetNickname + "】发送过组队邀请，请等待对方回应。");
      }
    }

    TeamInvitation invitation = new TeamInvitation();
    invitation.setTeamId(team.getId());
    invitation.setInviterId(userId);
    invitation.setInviteeId(invitee.getId());
    invitation.setStatus(InvitationStatus.PENDING);
    invitation.setCreatedAt(TimeUtil.now());
    invitation.setExpiresAt(TimeUtil.now().plusMinutes(5));
    invitationRepository.save(invitation);

    log.info("玩家 {} 邀请 {} 组队, 邀请ID={}", userId, invitee.getId(), invitation.getId());
    return ("已向【" + targetNickname + "】发出组队邀请（编号 #" + invitation.getId() + "，5分钟内有效）。");
  }

  public String acceptInvitationInternal(Long userId, String invitationIdStr) {
    userStateService.loadUser(userId);

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

    Team team =
        teamRepository
            .findById(invitation.getTeamId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        ErrorCode.TEAM_INVITATION_NOT_FOUND, invitation.getTeamId()));
    if (!team.isActive()) {
      throw new BusinessException(ErrorCode.TEAM_INVITATION_EXPIRED);
    }

    TeamMember member = new TeamMember();
    member.setTeamId(team.getId());
    member.setUserId(userId);
    member.setJoinedAt(TimeUtil.now());
    teamMemberRepository.save(member);

    teamRepository.incrementMemberCount(team.getId());

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

  public String rejectInvitationInternal(Long userId, String invitationIdStr) {
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

  public String leaveTeamInternal(Long userId) {
    userStateService.loadUser(userId);

    Optional<TeamMember> memberOpt = teamMemberRepository.findByUserId(userId);
    if (memberOpt.isEmpty()) {
      throw new BusinessException(ErrorCode.TEAM_NOT_IN);
    }

    TeamMember member = memberOpt.get();
    Team team =
        teamRepository
            .findById(member.getTeamId())
            .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_IN));

    if (team.getLeaderId().equals(userId)) {
      teamMemberRepository.deleteByTeamId(team.getId());
      team.setMemberCount(0);
      team.disband();
      teamRepository.save(team);
      log.info("队长 {} 离开队伍 {}，队伍解散", userId, team.getId());
      return "你已离开队伍。队伍已解散。";
    }

    teamMemberRepository.deleteById(member.getId());
    teamRepository.decrementMemberCount(team.getId());

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
      Team team =
          teamRepository
              .findById(member.get().getTeamId())
              .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_IN));
      if (team.isActive()) {
        throw new BusinessException(ErrorCode.TEAM_ALREADY_IN, team.getId());
      }
    }

    Team team = new Team();
    team.setLeaderId(userId);
    team.setMemberCount(1);
    team.setStatus(TeamStatus.ACTIVE);
    team.setCreatedAt(TimeUtil.now());
    teamRepository.save(team);

    TeamMember selfMember = new TeamMember();
    selfMember.setTeamId(team.getId());
    selfMember.setUserId(userId);
    selfMember.setJoinedAt(TimeUtil.now());
    teamMemberRepository.save(selfMember);

    return team;
  }
}
