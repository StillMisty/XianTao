package top.stillmisty.xiantao.service.player.state;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;
import top.stillmisty.xiantao.service.GameEventService;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
class HpRecoveryHandler implements StateHandler {

  private static final long HP_RECOVERY_INTERVAL_MINUTES = 5;

  private final GameEventService gameEventService;

  @Override
  public boolean tryResolve(User user) {
    if (user.getStatus() != UserStatus.IDLE) return false;

    int maxHp = user.calculateMaxHp();
    if (user.getHpCurrent() >= maxHp) return false;

    LocalDateTime now = TimeUtil.now();
    LocalDateTime lastRecovery = user.getLastHpRecoveryTime();
    if (lastRecovery == null) {
      user.setLastHpRecoveryTime(now);
      return true;
    }

    long elapsedMinutes = Duration.between(lastRecovery, now).toMinutes();
    long ticks = elapsedMinutes / HP_RECOVERY_INTERVAL_MINUTES;
    if (ticks <= 0) return false;

    for (long i = 0; i < ticks; i++) {
      user.naturalHpRecovery();
    }

    user.setLastHpRecoveryTime(lastRecovery.plusMinutes(ticks * HP_RECOVERY_INTERVAL_MINUTES));

    if (user.getHpCurrent() >= maxHp) {
      gameEventService.save(
          GameEvent.create(user.getId(), GameEventCategory.HP_RECOVERED)
              .withNarrative("你的生命值已完全恢复。", null));
    }

    log.debug("玩家 {} HP 自然恢复 {} 格，当前 {}/{}", user.getId(), ticks, user.getHpCurrent(), maxHp);
    return true;
  }
}
