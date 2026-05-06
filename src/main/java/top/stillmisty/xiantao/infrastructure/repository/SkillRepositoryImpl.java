package top.stillmisty.xiantao.infrastructure.repository;

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
}
