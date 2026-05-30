package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class HealFlatEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.HEAL_FLAT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.AmountParams p)) return Map.of();
    long heal = p.resolveAmount();
    if (heal <= 0) return Map.of();
    int newHp = Math.min(user.calculateMaxHp(), user.getHpCurrent() + (int) heal);
    user.setHpCurrent(newHp);
    return Map.of("heal", heal, "hpCurrent", newHp);
  }
}
