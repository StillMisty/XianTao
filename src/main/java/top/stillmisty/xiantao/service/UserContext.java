package top.stillmisty.xiantao.service;

/** 用户上下文持有者 用于在 Function Calling 中传递当前用户 ID */
public class UserContext {

  private static final ScopedValue<Long> CURRENT_USER = ScopedValue.newInstance();

  /**
   * ScopedValue 降级通道 —— ScopedValue 在 {@code @Authenticated} 方法返回后自动解绑，<br>
   * 下游模块（如 NotificationAppender）通过此 ThreadLocal 获取 userId，无需重复认证。<br>
   * 语义：每个请求绑定到一个虚拟线程，ThreadLocal 随线程销毁自然清理。
   */
  private static final ThreadLocal<Long> DOWNSTREAM_USER = new ThreadLocal<>();

  /** 获取当前用户 ID，未绑定时返回 null */
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

  /** 获取当前用户 ID：优先 ScopedValue（auth 方法内），次选 ThreadLocal（下游模块） */
  public static Long getUserId() {
    if (CURRENT_USER.isBound()) return CURRENT_USER.get();
    return DOWNSTREAM_USER.get();
  }

  /** 在指定 userId 上下文中执行回调 */
  public static <T, X extends Throwable> T withUser(Long userId, ScopedValue.CallableOp<T, X> op)
      throws X {
    return ScopedValue.where(CURRENT_USER, userId).call(op);
  }

  /** 设置下游 ThreadLocal，由 AuthenticatedAspect 在绑定 ScopedValue 前调用 */
  public static void setDownstreamUserId(Long userId) {
    DOWNSTREAM_USER.set(userId);
  }
}
