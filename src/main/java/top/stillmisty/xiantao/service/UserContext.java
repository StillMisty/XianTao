package top.stillmisty.xiantao.service;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.jspecify.annotations.Nullable;

/** 用户上下文持有者 用于在 Function Calling 中传递当前用户 ID */
public class UserContext {

  private static final ScopedValue<Long> CURRENT_USER = ScopedValue.newInstance();

  /**
   * 跨线程传播的用户 ID 映射。
   *
   * <p>AuthInterceptor 运行在原始线程，而 handler 运行在 IO 线程（通过 {@code runInNoScopeBlocking}）。ScopedValue 在
   * IO 线程上不可见，因此需要通过事件对象作为 key 在 WeakHashMap 中暂存 userId，再由 handler 侧读取并在 IO 线程上绑定。 WeakHashMap
   * 确保事件处理完成后，条目随事件对象 GC 自动清理。
   */
  private static final Map<Object, Long> EVENT_USER_MAP =
      Collections.synchronizedMap(new WeakHashMap<>());

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

  /** 为事件绑定 userId，仅当尚未绑定时。 保证同一事件只执行一次认证。 */
  @Nullable
  public static Long bindIfAbsent(Object event, Long userId) {
    synchronized (EVENT_USER_MAP) {
      Long existing = EVENT_USER_MAP.get(event);
      if (existing != null) return existing;
      EVENT_USER_MAP.put(event, userId);
      return null;
    }
  }

  /** 获取事件绑定的 userId（在 IO 线程上调用） */
  @Nullable
  public static Long retrieveFromEvent(Object event) {
    return EVENT_USER_MAP.get(event);
  }

  /** 解绑事件的 userId。WeakHashMap 会自动清理，此方法仅用于主动清除。 */
  public static void unbindForEvent(Object event) {
    EVENT_USER_MAP.remove(event);
  }

  // ==================== GM 检查缓存 ====================

  private static final Map<Object, Boolean> GM_CHECK_CACHE =
      Collections.synchronizedMap(new WeakHashMap<>());

  /** 获取缓存的 GM 检查结果。未缓存返回 null。 */
  @Nullable
  public static Boolean getGmCheck(Object event) {
    return GM_CHECK_CACHE.get(event);
  }

  /**
   * 原子性地绑定 GM 检查结果。
   *
   * @return 已有值时返回已有值，成功写入时返回 null
   */
  @Nullable
  public static Boolean gmCheckIfAbsent(Object event, boolean isGm) {
    synchronized (GM_CHECK_CACHE) {
      Boolean existing = GM_CHECK_CACHE.get(event);
      if (existing != null) return existing;
      GM_CHECK_CACHE.put(event, isGm);
      return null;
    }
  }
}
