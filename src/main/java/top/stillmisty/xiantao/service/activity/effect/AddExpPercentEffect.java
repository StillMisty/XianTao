package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class AddExpPercentEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_EXP_PERCENT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    double percent = ((Number) params.get("percent")).doubleValue();
    long exp = (long) (user.calculateExpToNextLevel() * percent);
    user.addExp(exp);
    return Map.of("exp", exp);
  }
}
