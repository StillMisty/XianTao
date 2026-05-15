package top.stillmisty.xiantao.domain.team.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.team.entity.TeamInvitation;

public interface TeamInvitationRepository {
  Optional<TeamInvitation> findById(Long id);

  List<TeamInvitation> findPendingByInviteeId(Long inviteeId);

  List<TeamInvitation> findPendingByTeamId(Long teamId);

  TeamInvitation save(TeamInvitation invitation);
}
