package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectSkill;
import top.stillmisty.xiantao.domain.sect.repository.SectSkillRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SectSkillMapper;

@Repository
@RequiredArgsConstructor
public class SectSkillRepositoryImpl implements SectSkillRepository {

  private final SectSkillMapper sectSkillMapper;

  @Override
  public SectSkill save(SectSkill skill) {
    sectSkillMapper.insertOrUpdateSelective(skill);
    return skill;
  }

  @Override
  public List<SectSkill> findBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectSkill::getSectId, sectId);
    return sectSkillMapper.selectListByQuery(query);
  }

  @Override
  public void deleteBySectId(Long sectId) {
    QueryWrapper query = new QueryWrapper().eq(SectSkill::getSectId, sectId);
    sectSkillMapper.deleteByQuery(query);
  }
}
