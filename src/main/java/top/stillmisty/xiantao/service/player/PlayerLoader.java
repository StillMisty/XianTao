package top.stillmisty.xiantao.service.player;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.infrastructure.repository.UserRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;

/**
 * 玩家数据加载（不含状态结算）。
 *
 * <p>只依赖 {@link UserRepository}，位于依赖图叶子：任何服务都可以安全注入，不会参与构造器循环依赖。 过期状态的结算由 {@link UserStateService}
 * 在命令边界（{@code CommandDispatcher}）统一触发，深层服务只做纯数据加载。
 */
@Component
@RequiredArgsConstructor
public class PlayerLoader {

  private final UserRepository userRepository;

  /** 行锁加载（{@code SELECT ... FOR UPDATE}），不触发状态结算；结算由 {@link UserStateService#settle} 在命令边界执行。 */
  public Player load(Long userId) {
    return userRepository
        .findByIdForUpdate(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  /** 只读加载：不加锁、不结算。 */
  public Player loadReadOnly(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  /** 按道号查询，可能不存在。 */
  public @Nullable Player findByNickname(String nickname) {
    return userRepository.findByNickname(nickname).orElse(null);
  }
}
