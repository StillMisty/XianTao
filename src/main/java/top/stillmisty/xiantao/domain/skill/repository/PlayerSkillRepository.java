package top.stillmisty.xiantao.domain.skill.repository;

import java.util.List;
import java.util.Optional;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;

public interface PlayerSkillRepository {

  List<PlayerSkill> findByUserId(Long userId);

  List<PlayerSkill> findEquippedByUserId(Long userId);

  Optional<PlayerSkill> findByUserIdAndSkillId(Long userId, Long skillId);

  PlayerSkill save(PlayerSkill playerSkill);

  void deleteById(Long id);
}
