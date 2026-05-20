package top.stillmisty.xiantao.service.activity.effect;

import java.util.Map;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.User;

@Component
public class PureNarrativeEffect implements SubEventEffect {

  @Override
  public SubEventEffectType type() {
    return SubEventEffectType.PURE_NARRATIVE;
  }

  @Override
  public Map<String, Object> execute(
      Long userId, User user, Map<String, Object> params, Map<String, Object> context) {
    return Map.of();
  }
}
