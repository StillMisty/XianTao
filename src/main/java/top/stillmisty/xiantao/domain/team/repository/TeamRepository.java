package top.stillmisty.xiantao.domain.team.repository;

import java.util.Optional;
import top.stillmisty.xiantao.domain.team.entity.Team;

public interface TeamRepository {
  Optional<Team> findById(Long id);

  Optional<Team> findByLeaderIdAndStatus(Long leaderId, String status);

  Team save(Team team);

  void deleteById(Long id);
}
