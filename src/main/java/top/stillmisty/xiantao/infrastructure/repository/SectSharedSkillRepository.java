package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.sect.entity.table.SectSharedSkillTableDef.SECT_SHARED_SKILL;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.SectSharedSkill;
import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;
import top.stillmisty.xiantao.infrastructure.mapper.SectSharedSkillMapper;

@Repository
@RequiredArgsConstructor
public class SectSharedSkillRepository {

  private final SectSharedSkillMapper mapper;

  public SectSharedSkill save(SectSharedSkill skill) {
    mapper.insertOrUpdateSelective(skill);
    return skill;
  }

  public Optional<SectSharedSkill> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  public List<SectSharedSkill> findBySectId(Long sectId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_SHARED_SKILL.SECT_ID.eq(sectId));
    return mapper.selectListByQuery(query);
  }

  public List<SectSharedSkill> findBySectIdAndStatus(Long sectId, SectSharedSkillStatus status) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(SECT_SHARED_SKILL.SECT_ID.eq(sectId))
            .and(SECT_SHARED_SKILL.STATUS.eq(status));
    return mapper.selectListByQuery(query);
  }

  public Optional<SectSharedSkill> findBySectIdAndSkillId(Long sectId, Long skillId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(SECT_SHARED_SKILL.SECT_ID.eq(sectId))
            .and(SECT_SHARED_SKILL.SKILL_ID.eq(skillId));
    return Optional.ofNullable(mapper.selectOneByQuery(query));
  }

  public long countBySectIdAndStatus(Long sectId, SectSharedSkillStatus status) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(SECT_SHARED_SKILL.SECT_ID.eq(sectId))
            .and(SECT_SHARED_SKILL.STATUS.eq(status));
    return mapper.selectCountByQuery(query);
  }

  public void deleteById(Long id) {
    mapper.deleteById(id);
  }

  public void deleteBySectId(Long sectId) {
    QueryWrapper query = QueryWrapper.create().where(SECT_SHARED_SKILL.SECT_ID.eq(sectId));
    mapper.deleteByQuery(query);
  }
}
