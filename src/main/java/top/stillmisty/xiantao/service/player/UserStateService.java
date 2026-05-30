package top.stillmisty.xiantao.service.player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.player.state.StateHandler;

/** 用户状态服务 — 加载用户实体并委托 {@link StateHandler} 列表自动结算过期的运行时状态。 */
@Slf4j
@Service
public class UserStateService {

  private final UserRepository userRepository;
  private final List<StateHandler> stateHandlers;

  public UserStateService(UserRepository userRepository, List<StateHandler> stateHandlers) {
    this.userRepository = userRepository;
    this.stateHandlers = stateHandlers;
  }

  /** 加载用户并自动结算过期状态。使用行锁防止并发状态更新。 */
  @Transactional
  public User loadUser(Long userId) {
    User user =
        userRepository
            .findByIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    resolveState(user);
    return user;
  }

  /** 只读加载用户，不获取行锁、不结算状态。适用于仅需读取用户数据的场景。 */
  public User loadUserReadOnly(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  /** 批量加载用户（只读），不获取行锁、不结算状态。 */
  public Map<Long, User> loadUsersByIdsReadOnly(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) return Map.of();
    return userRepository.findByIds(userIds).stream()
        .collect(Collectors.toMap(User::getId, u -> u));
  }

  /** 批量加载用户（带行锁），并自动结算过期状态。 */
  @Transactional
  public Map<Long, User> loadUsersByIds(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) return Map.of();
    Map<Long, User> userMap =
        userRepository.findByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
    // 对每个用户结算状态
    for (User user : userMap.values()) {
      resolveState(user);
    }
    return userMap;
  }

  /** 根据道号加载用户，不结算状态。 */
  public User loadUserByNickname(String nickname) {
    return userRepository.findByNickname(nickname).orElse(null);
  }

  /** 保存用户（全字段）。 */
  @Transactional
  public User save(User user) {
    return userRepository.save(user);
  }

  /** 清除活动标记，回空闲状态。 */
  @Transactional
  public void clearActivity(Long userId) {
    userRepository.clearActivity(userId);
  }

  /** 仅保存状态/活动相关字段（不碰灵石等数据字段）。 */
  @Transactional
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
  @Transactional
  public void saveHpStatus(User user) {
    userRepository.updateHpStatus(
        user.getId(), user.getHpCurrent(), user.getStatus().getCode(), user.getDyingStartTime());
  }

  /** 历练结算后持久化：HP、修为、状态、濒死时间、活动字段。 */
  @Transactional
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

  // ===================== 状态结算 =====================

  private void resolveState(User user) {
    boolean dirty = false;
    for (StateHandler handler : stateHandlers) {
      dirty |= handler.tryResolve(user);
    }
    if (dirty) {
      userRepository.save(user);
    }
  }
}
