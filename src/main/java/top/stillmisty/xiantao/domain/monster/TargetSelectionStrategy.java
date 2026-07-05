package top.stillmisty.xiantao.domain.monster;

import org.jspecify.annotations.Nullable;

/**
 * 目标选择策略
 *
 * <p>控制防守方如何选择攻击目标。默认 PvE 策略选择最低血量比例目标。
 */
@FunctionalInterface
public interface TargetSelectionStrategy {

  /**
   * 从防守方队伍中选择目标
   *
   * @param defenderTeam 防守方队伍
   * @return 选中的目标，或 null 表示无可攻击目标
   */
  @Nullable Combatant selectTarget(CombatTeam defenderTeam);
}
