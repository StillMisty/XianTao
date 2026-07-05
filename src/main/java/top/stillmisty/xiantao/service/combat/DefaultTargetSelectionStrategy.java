package top.stillmisty.xiantao.service.combat;

import java.util.Comparator;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.monster.CombatTeam;
import top.stillmisty.xiantao.domain.monster.Combatant;
import top.stillmisty.xiantao.domain.monster.TargetSelectionStrategy;

/** 默认目标选择策略：选择存活目标中血量比例最低的 */
@Component
public class DefaultTargetSelectionStrategy implements TargetSelectionStrategy {

  @Override
  public @Nullable Combatant selectTarget(CombatTeam defenderTeam) {
    var alive = defenderTeam.aliveMembers();
    if (alive.isEmpty()) return null;
    return alive.stream()
        .min(Comparator.comparingDouble(c -> (double) c.getHp() / c.getMaxHp()))
        .orElse(alive.getFirst());
  }
}
