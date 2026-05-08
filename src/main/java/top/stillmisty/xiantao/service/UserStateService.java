package top.stillmisty.xiantao.service;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.activity.TravelCompleter;

/** 用户状态服务 统一入口：加载用户实体并自动解析过期的运行时状态（旅行结算、HP 恢复、buff 清理等）。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStateService {

  private static final long HP_RECOVERY_INTERVAL_MINUTES = 5;
  private static final long DYING_RECOVERY_TIMEOUT_MINUTES = 30;

  private final UserRepository userRepository;
  private final MapNodeRepository mapNodeRepository;
  private final PlayerBuffRepository playerBuffRepository;
  private final GameEventService gameEventService;
  private final TravelCompleter travelCompleter;

  /** 加载用户并自动解析过期状态。 */
  public User loadUser(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    resolveState(user);
    return user;
  }

  /** 保存用户。 */
  public User save(User user) {
    return userRepository.save(user);
  }

  // ===================== 状态解析 =====================

  private void resolveState(User user) {
    boolean dirty = false;

    dirty |= tryCompleteTravel(user);
    dirty |= tryDyingRecovery(user);
    dirty |= tryHpRecovery(user);
    dirty |= tryExpireBuffs(user);

    if (dirty) {
      userRepository.save(user);
    }
  }

  /** 旅行时间已到 → 自动到达目的地，恢复 IDLE，产出事件 */
  private boolean tryCompleteTravel(User user) {
    if (user.getStatus() != UserStatus.TRAVELING) return false;
    if (user.getActivityType() != ActivityType.TRAVEL) return false;
    if (user.getActivityTargetId() == null) return false;

    var startTime = user.getActivityStartTime();
    if (startTime == null) {
      // Broken state: clear activity
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      return true;
    }

    var currentMap = mapNodeRepository.findById(user.getLocationId());
    if (currentMap.isEmpty()) return false;

    var destinationMap = mapNodeRepository.findById(user.getActivityTargetId());
    if (destinationMap.isEmpty()) return false;

    Integer travelTime = currentMap.get().getTravelTimeTo(destinationMap.get().getId());
    if (travelTime == null) return false;

    LocalDateTime arrivalTime = startTime.plusMinutes(travelTime);
    if (LocalDateTime.now().isBefore(arrivalTime)) return false;

    log.info(
        "玩家 {} 旅行自动结算：{} → {}",
        user.getId(),
        currentMap.get().getName(),
        destinationMap.get().getName());

    user.setStatus(UserStatus.IDLE);
    user.setLocationId(user.getActivityTargetId());
    user.clearActivity();

    // 产出旅行到达事件（叙事 + 子事件 + 隐藏事件）
    travelCompleter.completeTravel(user.getId(), user, currentMap.get(), destinationMap.get());

    return true;
  }

  /** HP 自然恢复：空闲时每 5 分钟恢复 1% 最大 HP */
  private boolean tryHpRecovery(User user) {
    if (user.getStatus() != UserStatus.IDLE) return false;

    int maxHp = user.calculateMaxHp();
    if (user.getHpCurrent() >= maxHp) return false;

    LocalDateTime now = LocalDateTime.now();
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

    // If HP is now full, produce event
    if (user.getHpCurrent() >= maxHp) {
      gameEventService.createEvent(
          user.getId(), GameEventCategory.HP_RECOVERED, "你的生命值已完全恢复。", null);
    }

    log.debug("玩家 {} HP 自然恢复 {} 格，当前 {}/{}", user.getId(), ticks, user.getHpCurrent(), maxHp);
    return true;
  }

  /** 清理该用户已过期的 buff */
  private boolean tryExpireBuffs(User user) {
    playerBuffRepository.deleteExpiredByUserId(user.getId());
    return false;
  }

  /** DYING 超时恢复：濒死状态超过 30 分钟后自动恢复 20% HP 并回到 IDLE */
  private boolean tryDyingRecovery(User user) {
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

    gameEventService.createEvent(
        user.getId(),
        GameEventCategory.DYING_RECOVERED,
        "你从重伤中恢复了过来，HP 恢复到 {{hp}}。",
        java.util.Map.of("hp", recoveryHp));

    log.info("玩家 {} 濒死超时自动恢复，HP 恢复到 {}", user.getId(), recoveryHp);
    return true;
  }
}
