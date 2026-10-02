package top.stillmisty.xiantao.service.player;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.player.state.StateHandler;

/** 用户状态结算 — 在命令边界统一触发 {@link StateHandler} 列表结算过期的运行时状态。 */
@Slf4j
@Service
public class UserStateService {

  private final UserRepository userRepository;
  private final List<StateHandler> stateHandlers;

  public UserStateService(UserRepository userRepository, List<StateHandler> stateHandlers) {
    this.userRepository = userRepository;
    this.stateHandlers = stateHandlers;
  }

  /**
   * 在命令边界统一结算过期状态（幂等；稳定状态走快速路径跳过）。
   *
   * <p>结算职责收敛于边界后，深层服务只使用 {@link PlayerLoader} 做纯数据加载、{@link PlayerWriter} 做显式写入， 避免「服务 → 结算中枢 →
   * StateHandler → 服务」的构造器循环依赖。
   */
  @Transactional
  public void settle(Long userId) {
    resolveState(
        userRepository
            .findByIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)));
  }

  private void resolveState(Player user) {
    // 快速路径：稳定状态下跳过所有 handler
    if (user.isStableState()) return;

    boolean dirty = false;
    for (StateHandler handler : stateHandlers) {
      dirty |= handler.tryResolve(user);
    }
    if (dirty) {
      userRepository.save(user);
    }
  }
}
