package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    Number amountNum = (Number) params.get("amount");
    if (amountNum == null) return Map.of();
    long stones = amountNum.longValue();
    spiritStoneService.withdraw(userId, stones);
    return Map.of("spiritStones", -stones);
  }
}
