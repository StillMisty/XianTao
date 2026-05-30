package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class TakeDamagePercentEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.TAKE_DAMAGE_PERCENT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.PercentParams p)) return Map.of();
    if (p.percent() == null) return Map.of();
    int damage = (int) (user.calculateMaxHp() * p.percent());
    user.takeDamage(damage);
    return Map.of("damage", damage, "hpCurrent", user.getHpCurrent());
  }
}
