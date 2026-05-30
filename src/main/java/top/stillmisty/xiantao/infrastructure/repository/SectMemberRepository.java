package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.sect.entity.table.SectMemberTableDef.SECT_MEMBER;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectMember;
import top.stillmisty.xiantao.infrastructure.mapper.SectMemberMapper;

@Repository
@RequiredArgsConstructor
public class SectMemberRepository {

  private final SectMemberMapper sectMemberMapper;

  public SectMember save(SectMember member) {
    sectMemberMapper.insertOrUpdateSelective(member);
    return member;
  }

  public Optional<SectMember> findById(Long id) {
    return Optional.ofNullable(sectMemberMapper.selectOneById(id));
  }

  public Optional<SectMember> findByUserId(Long userId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_MEMBER.USER_ID.eq(userId));
    return Optional.ofNullable(sectMemberMapper.selectOneByQuery(query));
  }

  public List<SectMember> findBySectId(Long sectId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_MEMBER.SECT_ID.eq(sectId));
    return sectMemberMapper.selectListByQuery(query);
  }

  public long countBySectId(Long sectId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_MEMBER.SECT_ID.eq(sectId));
    return sectMemberMapper.selectCountByQuery(query);
  }

  public void deleteById(Long id) {
    sectMemberMapper.deleteById(id);
  }

  public void deleteByUserId(Long userId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_MEMBER.USER_ID.eq(userId));
    sectMemberMapper.deleteByQuery(query);
  }
}
