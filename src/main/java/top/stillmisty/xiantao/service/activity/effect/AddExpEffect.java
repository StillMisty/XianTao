package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class AddExpEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_EXP;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    long exp = resolveAmount(params);
    user.addExp(exp);
    return Map.of("exp", exp);
  }

  static long resolveAmount(Map<String, Object> params) {
    if (params.containsKey("amount")) return ((Number) params.get("amount")).longValue();
    long min = params.containsKey("min") ? ((Number) params.get("min")).longValue() : 0;
    long max = params.containsKey("max") ? ((Number) params.get("max")).longValue() : 0;
    return ThreadLocalRandom.current().nextLong(min, max + 1);
  }
}
