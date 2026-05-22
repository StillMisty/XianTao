package top.stillmisty.xiantao.service.player;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.event.enums.ActivityType;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.FortuneService;
import top.stillmisty.xiantao.service.GameEventService;
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
  private final FortuneService fortuneService;

  /** 加载用户并自动解析过期状态。使用行锁防止并发状态更新。 */
  @Transactional
  public User loadUser(Long userId) {
    User user =
        userRepository
            .findByIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    resolveState(user);
    return user;
  }

  public User loadUserForUpdate(Long userId) {
    User user =
        userRepository
            .findByIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    if (tryDailyFortune(user)) {
      userRepository.save(user);
    }
    return user;
  }

  /** 根据道号加载用户，不解析状态。 */
  public User loadUserByNickname(String nickname) {
    return userRepository.findByNickname(nickname).orElse(null);
  }

  /** 保存用户（全字段）。 */
  public User save(User user) {
    return userRepository.save(user);
  }

  /** 清除活动标记，回空闲状态。 */
  public void clearActivity(Long userId) {
    userRepository.clearActivity(userId);
  }

  /** 仅保存状态/活动相关字段（不碰灵石等数据字段）。 */
  public void saveActivity(User user) {
    if (user.getActivityType() == null) {
      userRepository.clearActivity(user.getId());
    } else {
      userRepository.startActivity(
          user.getId(),
          user.getStatus().getCode(),
          user.getActivityType().getCode(),
          user.getActivityStartTime(),
          user.getActivityTargetId());
    }
  }

  /** 仅保存 HP/状态/濒死时间。 */
  public void saveHpStatus(User user) {
    userRepository.updateHpStatus(
        user.getId(), user.getHpCurrent(), user.getStatus().getCode(), user.getDyingStartTime());
  }

  /** 历练结算后持久化：HP、修为、状态、濒死时间、活动字段。 */
  public void saveTrainingEndState(User user) {
    userRepository.completeTraining(
        user.getId(),
        user.getHpCurrent(),
        user.getExp(),
        user.getStatus().getCode(),
        user.getDyingStartTime(),
        user.getActivityType() != null ? user.getActivityType().getCode() : null,
        user.getActivityStartTime(),
        user.getActivityTargetId());
  }

  // ===================== 状态解析 =====================

  private void resolveState(User user) {
    boolean dirty = false;

    dirty |= tryCompleteTravel(user);
    dirty |= tryDyingRecovery(user);
    dirty |= tryHpRecovery(user);
    dirty |= tryExpireBuffs(user);
    dirty |= tryDailyFortune(user);

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
    if (travelTime == null) {
      // Broken state: clear travel activity to prevent softlock
      user.setStatus(UserStatus.IDLE);
      user.clearActivity();
      log.warn(
          "玩家 {} 旅行卡死检测，无路径 {} → {}，已清除状态",
          user.getId(),
          currentMap.get().getName(),
          destinationMap.get().getName());
      return true;
    }

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

  private boolean tryDailyFortune(User user) {
    LocalDate today = LocalDate.now();
    if (today.equals(user.getLastFortuneDate())) return false;

    user.setLastFortuneDate(today);
    String display = fortuneService.buildDisplay(user.getId());
    gameEventService.createEvent(
        user.getId(),
        GameEventCategory.FORTUNE,
        "{{fortuneText}}",
        java.util.Map.of("fortuneText", display));
    return true;
  }
}
