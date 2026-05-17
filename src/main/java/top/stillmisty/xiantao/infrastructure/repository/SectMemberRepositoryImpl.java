package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.domain.sect.repository.SectMemberRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SectMemberMapper;

@Repository
@RequiredArgsConstructor
public class SectMemberRepositoryImpl implements SectMemberRepository {

  private final SectMemberMapper sectMemberMapper;

  @Override
  public SectMember save(SectMember member) {
    sectMemberMapper.insertOrUpdateSelective(member);
    return member;
  }

  @Override
  public Optional<SectMember> findById(Long id) {
    return Optional.ofNullable(sectMemberMapper.selectOneById(id));
  }

  @Override
  public Optional<SectMember> findByUserId(Long userId) {
    QueryWrapper query = new QueryWrapper().eq(SectMember::getUserId, userId);
    return Optional.ofNullable(sectMemberMapper.selectOneByQuery(query));
  }

  @Override
  public List<SectMember> findBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectMember::getSectId, sectId);
    return sectMemberMapper.selectListByQuery(query);
  }

  @Override
  public long countBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectMember::getSectId, sectId);
    return sectMemberMapper.selectCountByQuery(query);
  }

  @Override
  public void deleteById(Long id) {
    sectMemberMapper.deleteById(id);
  }

  @Override
  public void deleteByUserId(Long userId) {
    QueryWrapper query = new QueryWrapper().eq(SectMember::getUserId, userId);
    sectMemberMapper.deleteByQuery(query);
  }
}
