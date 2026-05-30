package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.SpiritStoneService;

@Component
@RequiredArgsConstructor
public class TakeSpiritStonesEffect implements SubEventEffect {

  private final SpiritStoneService spiritStoneService;

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.TAKE_SPIRIT_STONES;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.AmountParams p)) return Map.of();
    long amount = p.resolveAmount();
    if (amount <= 0) return Map.of();
    spiritStoneService.withdraw(userId, amount);
    return Map.of("spiritStones", -amount);
  }
}
