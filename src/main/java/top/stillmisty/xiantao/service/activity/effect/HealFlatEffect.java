package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class HealFlatEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.HEAL_FLAT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    long heal = AddExpEffect.resolveAmount(params);
    int newHp = Math.min(user.calculateMaxHp(), user.getHpCurrent() + (int) heal);
    user.setHpCurrent(newHp);
    return Map.of("heal", heal, "hpCurrent", newHp);
  }
}
