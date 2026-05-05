package top.stillmisty.xiantao.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.map.repository.MapNodeRepository;
import top.stillmisty.xiantao.domain.pill.repository.PlayerBuffRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.UserStatus;
import top.stillmisty.xiantao.domain.user.repository.UserRepository;

/** 用户状态服务 统一入口：加载用户实体并自动解析过期的运行时状态（旅行结算、HP 恢复、buff 清理等）。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStateService {

  private static final long HP_RECOVERY_INTERVAL_MINUTES = 5;
  private static final long DYING_RECOVERY_TIMEOUT_MINUTES = 30;
  private static final String EXTRA_LAST_HP_RECOVERY = "lastHpRecovery";
  private static final String EXTRA_DYING_SINCE = "dyingSince";

  private final UserRepository userRepository;
  private final MapNodeRepository mapNodeRepository;
  private final PlayerBuffRepository playerBuffRepository;

  /** 加载用户并自动解析过期状态。 替代所有 userRepository.findById(userId).orElseThrow() 调用。 */
  public User getUser(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    resolveState(user);
    return user;
  }

  /** 保存用户。统一入口，后续可在此添加保存前后的钩子。 */
  public User save(User user) {
    return userRepository.save(user);
  }

  // ===================== 状态解析 =====================

  /** 按顺序解析所有可能过期的运行时状态。 每个方法内部做好早退判断，避免无效计算。 */
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

  /** 旅行时间已到 → 自动到达目的地，恢复 IDLE。 */
  private boolean tryCompleteTravel(User user) {
    if (user.getStatus() != UserStatus.RUNNING || user.getTravelDestinationId() == null) {
      return false;
    }
    if (user.getTravelStartTime() == null) {
      return false;
    }

    var currentMap = mapNodeRepository.findById(user.getLocationId());
    if (currentMap.isEmpty()) {
      return false;
    }

    var destinationMap = mapNodeRepository.findById(user.getTravelDestinationId());
    if (destinationMap.isEmpty()) {
      return false;
    }

    Integer travelTime = currentMap.get().getTravelTimeTo(destinationMap.get().getId());
    if (travelTime == null) {
      return false;
    }

    LocalDateTime arrivalTime = user.getTravelStartTime().plusMinutes(travelTime);
    if (LocalDateTime.now().isBefore(arrivalTime)) {
      return false;
    }

    log.info(
        "玩家 {} 旅行自动结算：{} → {}",
        user.getId(),
        currentMap.get().getName(),
        destinationMap.get().getName());
    user.setStatus(UserStatus.IDLE);
    user.setLocationId(user.getTravelDestinationId());
    user.setTravelStartTime(null);
    user.setTravelDestinationId(null);
    return true;
  }

  /** HP 自然恢复：空闲时每 5 分钟恢复 1% 最大 HP。 */
  private boolean tryHpRecovery(User user) {
    if (user.getStatus() != UserStatus.IDLE) {
      return false;
    }

    int maxHp = user.calculateMaxHp();
    if (user.getHpCurrent() >= maxHp) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastRecovery = readLastHpRecovery(user);
    if (lastRecovery == null) {
      writeLastHpRecovery(user, now);
      return true;
    }

    long elapsedMinutes = Duration.between(lastRecovery, now).toMinutes();
    long ticks = elapsedMinutes / HP_RECOVERY_INTERVAL_MINUTES;
    if (ticks <= 0) {
      return false;
    }

    for (long i = 0; i < ticks; i++) {
      user.naturalHpRecovery();
    }

    writeLastHpRecovery(user, lastRecovery.plusMinutes(ticks * HP_RECOVERY_INTERVAL_MINUTES));

    log.debug("玩家 {} HP 自然恢复 {} 格，当前 {}/{}", user.getId(), ticks, user.getHpCurrent(), maxHp);
    return true;
  }

  /** 清理该用户已过期的 buff。 SQL 查询已过滤 expires_at > NOW()，此方法清除过期残留以确保一致性。 */
  private boolean tryExpireBuffs(User user) {
    playerBuffRepository.deleteExpiredByUserId(user.getId());
    return false; // buff 操作不修改 user 实体，无需额外保存
  }

  /** DYING 超时恢复：濒死状态超过 30 分钟后自动恢复 20% HP 并回到 IDLE */
  private boolean tryDyingRecovery(User user) {
    if (user.getStatus() != UserStatus.DYING) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime dyingSince = readDyingSince(user);
    if (dyingSince == null) {
      writeDyingSince(user, now);
      return true;
    }

    long elapsedMinutes = Duration.between(dyingSince, now).toMinutes();
    if (elapsedMinutes < DYING_RECOVERY_TIMEOUT_MINUTES) {
      return false;
    }

    int recoveryHp = Math.max(1, user.calculateMaxHp() / 5);
    user.setHpCurrent(recoveryHp);
    user.setStatus(UserStatus.IDLE);
    user.setTrainingStartTime(null);
    clearDyingSince(user);

    log.info("玩家 {} 濒死超时自动恢复，HP 恢复到 {}", user.getId(), recoveryHp);
    return true;
  }

  // ===================== extraData 读写 =====================

  private LocalDateTime readLastHpRecovery(User user) {
    var extra = user.getExtraData();
    if (extra == null || !extra.containsKey(EXTRA_LAST_HP_RECOVERY)) {
      return null;
    }
    try {
      return LocalDateTime.parse(extra.get(EXTRA_LAST_HP_RECOVERY).toString());
    } catch (Exception e) {
      return null;
    }
  }

  private void writeLastHpRecovery(User user, LocalDateTime time) {
    var extra = user.getExtraData();
    if (extra == null) {
      extra = new HashMap<>();
    } else {
      extra = new HashMap<>(extra);
    }
    extra.put(EXTRA_LAST_HP_RECOVERY, time.toString());
    user.setExtraData(extra);
  }

  private LocalDateTime readDyingSince(User user) {
    var extra = user.getExtraData();
    if (extra == null || !extra.containsKey(EXTRA_DYING_SINCE)) {
      return null;
    }
    try {
      return LocalDateTime.parse(extra.get(EXTRA_DYING_SINCE).toString());
    } catch (Exception e) {
      return null;
    }
  }

  private void writeDyingSince(User user, LocalDateTime time) {
    var extra = user.getExtraData();
    if (extra == null) {
      extra = new HashMap<>();
    } else {
      extra = new HashMap<>(extra);
    }
    extra.put(EXTRA_DYING_SINCE, time.toString());
    user.setExtraData(extra);
  }

  private void clearDyingSince(User user) {
    var extra = user.getExtraData();
    if (extra == null || !extra.containsKey(EXTRA_DYING_SINCE)) {
      return;
    }
    extra = new HashMap<>(extra);
    extra.remove(EXTRA_DYING_SINCE);
    user.setExtraData(extra);
  }
}
