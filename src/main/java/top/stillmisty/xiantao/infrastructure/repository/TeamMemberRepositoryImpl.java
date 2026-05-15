package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.team.entity.table.TeamMemberTableDef.TEAM_MEMBER;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.team.entity.TeamMember;
import top.stillmisty.xiantao.domain.team.repository.TeamMemberRepository;
import top.stillmisty.xiantao.infrastructure.mapper.TeamMemberMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TeamMemberRepositoryImpl implements TeamMemberRepository {

  private final TeamMemberMapper mapper;

  @Override
  public Optional<TeamMember> findByUserId(Long userId) {
    return Optional.ofNullable(
        mapper.selectOneByQuery(
            QueryWrapper.create()
                .select()
                .from(TEAM_MEMBER)
                .where(TEAM_MEMBER.USER_ID.eq(userId))));
  }

  @Override
  public List<TeamMember> findByTeamId(Long teamId) {
    return mapper.selectListByQuery(
        QueryWrapper.create().select().from(TEAM_MEMBER).where(TEAM_MEMBER.TEAM_ID.eq(teamId)));
  }

  @Override
  public TeamMember save(TeamMember member) {
    mapper.insertOrUpdateSelective(member);
    return member;
  }

  @Override
  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  @Override
  public void deleteByTeamId(Long teamId) {
    mapper.deleteByQuery(
        QueryWrapper.create().from(TEAM_MEMBER).where(TEAM_MEMBER.TEAM_ID.eq(teamId)));
  }

  @Override
  public long countByTeamId(Long teamId) {
    return mapper.selectCountByQuery(
        QueryWrapper.create().select().from(TEAM_MEMBER).where(TEAM_MEMBER.TEAM_ID.eq(teamId)));
  }
}
