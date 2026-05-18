package top.stillmisty.xiantao.domain.skill.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

public interface SkillRepository {

  Optional<Skill> findById(Long id);

  List<Skill> findAll();

  List<Skill> findByIds(List<Long> ids);

  List<Skill> findByName(String name);

  Skill save(Skill skill);
}
