package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class AddExpPercentEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_EXP_PERCENT;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.PercentParams p)) return Map.of();
    if (p.percent() == null) return Map.of();
    long exp = (long) (user.calculateExpToNextLevel() * p.percent());
    user.addExp(exp);
    return Map.of("exp", exp);
  }
}
