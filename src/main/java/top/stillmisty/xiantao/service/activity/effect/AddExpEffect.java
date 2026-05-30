package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class AddExpEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_EXP;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.AmountParams p)) return Map.of();
    long exp = p.resolveAmount();
    user.addExp(exp);
    return Map.of("exp", exp);
  }
}
