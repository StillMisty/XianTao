package top.stillmisty.xiantao.service;

import org.jspecify.annotations.Nullable;

/** 用户上下文持有者 用于在 Function Calling 中传递当前用户 ID */
public class UserContext {

  private static final ScopedValue<Long> CURRENT_USER = ScopedValue.newInstance();

  /** 获取当前用户 ID，未绑定时返回 null */
  @Nullable
  public static Long getCurrentUserId() {
    return CURRENT_USER.isBound() ? CURRENT_USER.get() : null;
  }

  /** 获取当前用户 ID，未绑定时抛出 BusinessException */
  public static Long requireCurrentUserId() {
    Long userId = getCurrentUserId();
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_CONTEXT_MISSING);
    }
    return userId;
  }

  /** 在指定 userId 上下文中执行回调 */
  public static <T, X extends Throwable> T withUser(Long userId, ScopedValue.CallableOp<T, X> op)
      throws X {
    return ScopedValue.where(CURRENT_USER, userId).call(op);
  }
}
