package top.stillmisty.xiantao.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/** 用户上下文持有者 用于在 Function Calling 中传递当前用户 ID */
public class UserContext {

  private static final ScopedValue<Long> CURRENT_USER = ScopedValue.newInstance();

  /**
   * 跨线程传播的用户 ID 映射。
   *
   * <p>AuthInterceptor 运行在原始线程，而 handler 运行在 IO 线程（通过 {@code runInNoScopeBlocking}）。ScopedValue 在
   * IO 线程上不可见，因此需要通过事件对象作为 key 在 ConcurrentHashMap 中暂存 userId，再由 handler 侧读取并在 IO 线程上绑定。
   */
  private static final Map<Object, Long> EVENT_USER_MAP = new ConcurrentHashMap<>();

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

  // ==================== 跨线程传播 ====================

  /** 为事件绑定 userId（在原始线程上调用） */
  public static void bindForEvent(Object event, Long userId) {
    EVENT_USER_MAP.put(event, userId);
  }

  /** 解绑事件的 userId（在原始线程上清理） */
  public static void unbindForEvent(Object event) {
    EVENT_USER_MAP.remove(event);
  }

  /** 获取事件绑定的 userId（在 IO 线程上调用） */
  @Nullable
  public static Long retrieveFromEvent(Object event) {
    return EVENT_USER_MAP.get(event);
  }
}
