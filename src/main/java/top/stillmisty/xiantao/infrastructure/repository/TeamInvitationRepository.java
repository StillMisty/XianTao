package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.team.entity.table.TeamInvitationTableDef.TEAM_INVITATION;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.team.entity.TeamInvitation;
import top.stillmisty.xiantao.domain.team.enums.InvitationStatus;
import top.stillmisty.xiantao.infrastructure.mapper.TeamInvitationMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TeamInvitationRepository {

  private final TeamInvitationMapper mapper;

  public Optional<TeamInvitation> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<TeamInvitation> findPendingByInviteeId(Long inviteeId) {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .select()
            .from(TEAM_INVITATION)
            .where(TEAM_INVITATION.INVITEE_ID.eq(inviteeId))
            .and(TEAM_INVITATION.STATUS.eq(InvitationStatus.PENDING)));
  }

  public List<TeamInvitation> findPendingByTeamId(Long teamId) {
    return mapper.selectListByQuery(
        QueryWrapper.create()
            .select()
            .from(TEAM_INVITATION)
            .where(TEAM_INVITATION.TEAM_ID.eq(teamId))
            .and(TEAM_INVITATION.STATUS.eq(InvitationStatus.PENDING)));
  }

  public TeamInvitation save(TeamInvitation invitation) {
    mapper.insertOrUpdateSelective(invitation);
    return invitation;
  }
}
