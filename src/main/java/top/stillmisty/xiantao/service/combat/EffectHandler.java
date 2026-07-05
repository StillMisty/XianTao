package top.stillmisty.xiantao.service.combat;

import top.stillmisty.xiantao.domain.monster.BuffManager;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.skill.entity.Skill;
import top.stillmisty.xiantao.domain.skill.entity.SkillEffect;

/**
 * 技能效果处理器
 *
 * <p>每个 EffectType 对应一个 handler，通过 {@link EffectHandlerRegistry} 注册。 新增效果类型只需新建 handler
 * 实现并注册，无需修改引擎核心代码。
 */
@FunctionalInterface
public interface EffectHandler {

  /** 处理一个技能效果，返回效果结果 */
  EffectResult handle(EffectContext ctx);

  /** 效果上下文 */
  record EffectContext(
      Combatant attacker,
      Combatant defender,
      SkillEffect effect,
      Skill skill,
      BuffManager buffManager,
      CombatTeam defenderTeam,
      DamageCalculator damageCalculator) {}

  /** 效果处理结果 */
  record EffectResult(int damage, boolean isControl, boolean isBuff) {

    public static final EffectResult NONE = new EffectResult(0, false, false);
  }
}
