package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.team.entity.table.TeamTableDef.TEAM;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.team.entity.Team;
import top.stillmisty.xiantao.infrastructure.mapper.TeamMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TeamRepository {

  private final TeamMapper mapper;

  public Optional<Team> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public Optional<Team> findByLeaderIdAndStatus(Long leaderId, String status) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .select()
                .from(TEAM)
                .where(TEAM.LEADER_ID.eq(leaderId))
                .and(TEAM.STATUS.eq(status))));
  }

  public Team save(Team team) {
    mapper.insertOrUpdateSelective(team);
    return team;
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  public int incrementMemberCount(Long teamId) {
    return mapper.incrementMemberCount(teamId);
  }

  public int decrementMemberCount(Long teamId) {
    return mapper.decrementMemberCount(teamId);
  }
}
