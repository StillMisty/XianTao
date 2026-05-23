package top.stillmisty.xiantao.domain.skill.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

public interface SkillRepository {

  Optional<Skill> findById(Long id);

  List<Skill> findAll();

  List<Skill> findByIds(List<Long> ids);

  List<Skill> findByName(String name);

  /** 查询玩家可学习的技能（满足悟性、等级、无前置、未学习） */
  List<Skill> findLearnable(int wis, int level, Set<Long> excludeIds);

  Skill save(Skill skill);
}
