package top.stillmisty.xiantao.service.player.state;

import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;

@Component
@RequiredArgsConstructor
@Order(6)
class DailyFortuneHandler implements StateHandler {

  private final FortuneService fortuneService;
  private final GameEventService gameEventService;

  @Override
  public boolean tryResolve(User user) {
    LocalDate today = TimeUtil.today();
    if (today.equals(user.getLastFortuneDate())) return false;

    user.setLastFortuneDate(today);
    String display = fortuneService.buildDisplay(user.getId());
    gameEventService.save(
        GameEvent.create(user.getId(), GameEventCategory.FORTUNE)
            .withNarrative("{{fortuneText}}", Map.of("fortuneText", display)));
    return true;
  }
}
