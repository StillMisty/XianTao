package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.SpiritStoneService;

@Component
@RequiredArgsConstructor
public class AddSpiritStonesEffect implements SubEventEffect {

  private final SpiritStoneService spiritStoneService;

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.ADD_SPIRIT_STONES;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, EffectParams params, EventContext context) {
    if (!(params instanceof EffectParams.AmountParams p)) return Map.of();
    long stones = p.resolveAmount();
    if (stones <= 0) return Map.of();
    spiritStoneService.deposit(userId, stones);
    return Map.of("spiritStones", stones);
  }
}
