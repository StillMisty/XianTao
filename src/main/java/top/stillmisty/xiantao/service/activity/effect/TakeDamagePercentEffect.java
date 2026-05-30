package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class TakeDamagePercentEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.TAKE_DAMAGE_PERCENT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    Number amountNum = (Number) params.get("amount");
    if (amountNum == null) return Map.of();
    double amount = amountNum.doubleValue();
    int damage = (int) (user.calculateMaxHp() * amount);
    user.takeDamage(damage);
    return Map.of("damage", damage, "hpCurrent", user.getHpCurrent());
  }
}
