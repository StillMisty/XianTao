package top.stillmisty.xiantao.service.player.state;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.service.GameEventService;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
class DyingRecoveryHandler implements StateHandler {

  private static final long DYING_RECOVERY_TIMEOUT_MINUTES = 30;

  private final GameEventService gameEventService;

  @Override
  public boolean tryResolve(User user) {
    if (user.getStatus() != UserStatus.DYING) return false;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime dyingSince = user.getDyingStartTime();
    if (dyingSince == null) {
      user.setDyingStartTime(now);
      return true;
    }

    long elapsedMinutes = Duration.between(dyingSince, now).toMinutes();
    if (elapsedMinutes < DYING_RECOVERY_TIMEOUT_MINUTES) return false;

    int recoveryHp = Math.max(1, user.calculateMaxHp() / 5);
    user.setHpCurrent(recoveryHp);
    user.setStatus(UserStatus.IDLE);
    user.clearActivity();
    user.setDyingStartTime(null);

    gameEventService.save(
        GameEvent.create(user.getId(), GameEventCategory.DYING_RECOVERED)
            .withNarrative("你从重伤中恢复了过来，HP 恢复到 {{hp}}。", Map.of("hp", recoveryHp)));

    log.info("玩家 {} 濒死超时自动恢复，HP 恢复到 {}", user.getId(), recoveryHp);
    return true;
  }
}
