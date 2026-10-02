package top.stillmisty.xiantao.service.player;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;

/**
 * 玩家持久化（叶子组件，只依赖 {@link UserRepository}）。
 *
 * <p>读取走 {@link PlayerLoader}，过期状态结算走 {@link UserStateService#settle}（命令边界）；
 * 本组件只提供显式的写方法，深层服务注入它不会引入结算副作用或构造器环。
 */
@Component
@RequiredArgsConstructor
public class PlayerWriter {

  private final UserRepository userRepository;

  /** 保存用户（全字段，资金等原子列受实体注解保护）。 */
  @Transactional
  public Player save(Player user) {
    return userRepository.save(user);
  }

  /** 清除活动标记，回空闲状态。 */
  @Transactional
  public void clearActivity(Long userId) {
    userRepository.clearActivity(userId);
  }

  /** 仅保存状态/活动相关字段（不碰灵石等数据字段）。 */
  @Transactional
  public void saveActivity(Player user) {
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
  public void saveHpStatus(Player user) {
    userRepository.updateHpStatus(
        user.getId(), user.getHpCurrent(), user.getStatus().getCode(), user.getDyingStartTime());
  }

  /** 历练结算后持久化：HP、修为、状态、濒死时间、活动字段。 */
  @Transactional
  public void saveTrainingEndState(Player user) {
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
}
