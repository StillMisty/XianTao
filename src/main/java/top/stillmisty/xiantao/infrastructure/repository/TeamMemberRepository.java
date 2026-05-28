package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.team.entity.table.TeamMemberTableDef.TEAM_MEMBER;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.team.entity.TeamMember;
import top.stillmisty.xiantao.infrastructure.mapper.TeamMemberMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TeamMemberRepository {

  private final TeamMemberMapper mapper;

  public Optional<TeamMember> findByUserId(Long userId) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .select()
                .from(TEAM_MEMBER)
                .where(TEAM_MEMBER.USER_ID.eq(userId))));
  }

  public List<TeamMember> findByTeamId(Long teamId) {
    return mapper.selectListByQuery(
        QueryWrapper.create().select().from(TEAM_MEMBER).where(TEAM_MEMBER.TEAM_ID.eq(teamId)));
  }

  public TeamMember save(TeamMember member) {
    mapper.insertOrUpdateSelective(member);
    return member;
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  public void deleteByTeamId(Long teamId) {
    mapper.deleteByQuery(
        QueryWrapper.create().from(TEAM_MEMBER).where(TEAM_MEMBER.TEAM_ID.eq(teamId)));
  }

  public long countByTeamId(Long teamId) {
    return mapper.selectCountByQuery(
        QueryWrapper.create().select().from(TEAM_MEMBER).where(TEAM_MEMBER.TEAM_ID.eq(teamId)));
  }
}
