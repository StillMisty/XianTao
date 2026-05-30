package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.event.EventContextKeys;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class MultiplyBountyRewardEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.MULTIPLY_BOUNTY_REWARD;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.MultiplierParams p)) return Map.of();
    if (p.multiplier() == null) return Map.of();
    long[] holder = EventContextKeys.BOUNTY_REWARD.get(context);
    if (holder != null) {
      holder[0] = (long) (holder[0] * p.multiplier());
    }
    return Map.of("spiritStones", holder != null ? holder[0] : 0);
  }
}
