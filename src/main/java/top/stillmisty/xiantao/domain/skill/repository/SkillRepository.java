package top.stillmisty.xiantao.domain.skill.repository;

import top.stillmisty.xiantao.domain.skill.entity.Skill;

import java.util.List;
import java.util.Optional;

public interface SkillRepository {

    Optional<Skill> findById(Long id);

    List<Skill> findAll();

    List<Skill> findByIds(List<Long> ids);

    Skill save(Skill skill);
}
