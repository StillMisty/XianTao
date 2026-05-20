package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class TakeDamageFlatEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.TAKE_DAMAGE_FLAT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    int damage = (int) AddExpEffect.resolveAmount(params);
    user.takeDamage(damage);
    return Map.of("damage", damage, "hpCurrent", user.getHpCurrent());
  }
}
