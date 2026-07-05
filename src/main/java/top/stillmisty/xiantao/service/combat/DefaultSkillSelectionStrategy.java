package top.stillmisty.xiantao.service.combat;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.SkillSelectionStrategy;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

/** 默认技能选择策略：从可用技能中随机选择一个 */
@Component
public class DefaultSkillSelectionStrategy implements SkillSelectionStrategy {

  @Override
  public @Nullable Skill selectSkill(Combatant attacker, List<Skill> available) {
    if (available.isEmpty()) return null;
    return available.get(ThreadLocalRandom.current().nextInt(available.size()));
  }
}
