package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.EventContext;
import top.stillmisty.xiantao.domain.user.entity.Player;

@Component
public class PureNarrativeEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.PURE_NARRATIVE;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, Player user, EffectParams params, EventContext context) {
    return Map.of();
  }
}
