package top.stillmisty.xiantao.domain.monster;

import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.skill.entity.Skill;

/**
 * 技能选择策略
 *
 * <p>控制战斗者在可用技能中选择的行为。默认实现为随机选择。
 */
@FunctionalInterface
public interface SkillSelectionStrategy {

  /**
   * 从可用技能中选择一个
   *
   * @param attacker 攻击者
   * @param available 可用技能列表（不在冷却中且非沉默）
   * @return 选中的技能，或 null 表示普通攻击
   */
  @Nullable Skill selectSkill(Combatant attacker, List<Skill> available);
}
