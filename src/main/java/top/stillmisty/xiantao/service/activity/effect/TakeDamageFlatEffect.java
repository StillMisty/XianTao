package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class TakeDamageFlatEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.TAKE_DAMAGE_FLAT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.AmountParams p)) return Map.of();
    int damage = (int) p.resolveAmount();
    if (damage <= 0) return Map.of();
    user.takeDamage(damage);
    return Map.of("damage", damage, "hpCurrent", user.getHpCurrent());
  }
}
