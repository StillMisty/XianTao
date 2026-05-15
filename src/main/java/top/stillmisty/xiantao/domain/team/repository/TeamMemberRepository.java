package top.stillmisty.xiantao.domain.team.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.team.entity.TeamMember;

public interface TeamMemberRepository {
  Optional<TeamMember> findByUserId(Long userId);

  List<TeamMember> findByTeamId(Long teamId);

  TeamMember save(TeamMember member);

  void deleteById(Long id);

  void deleteByTeamId(Long teamId);

  long countByTeamId(Long teamId);
}
