package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.skill.entity.table.SkillTableDef.SKILL;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.repository.SkillRepository;
import top.stillmisty.xiantao.infrastructure.mapper.SkillMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SkillRepositoryImpl implements SkillRepository {

  private final SkillMapper mapper;

  @Override
  public Optional<Skill> findById(Long id) {
    return Optional.ofNullable(mapper.selectOneById(id));
  }

  @Override
  public List<Skill> findAll() {
    return mapper.selectAll();
  }

  @Override
  public List<Skill> findByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return mapper.selectListByIds(ids);
  }

  @Override
  public Skill save(Skill skill) {
    mapper.insertOrUpdateSelective(skill);
    return skill;
  }

  @Override
  public List<Skill> findByName(String name) {
    return mapper.selectListByQuery(QueryWrapper.create().where(SKILL.NAME.eq(name)));
  }
}
