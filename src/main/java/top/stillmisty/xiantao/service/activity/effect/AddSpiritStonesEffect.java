package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class AddSpiritStonesEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_SPIRIT_STONES;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    long stones = AddExpEffect.resolveAmount(params);
    user.setSpiritStones(user.getSpiritStones() + stones);
    return Map.of("spiritStones", stones);
  }
}
