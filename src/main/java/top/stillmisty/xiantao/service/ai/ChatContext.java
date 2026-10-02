package top.stillmisty.xiantao.service.ai;

import org.jspecify.annotations.Nullable;

/**
 * 对话上下文 — 一次对话在同一个 ScopedValue 槽位上绑定类型化上下文（店铺/地灵/秘境）。
 *
 * <p>绑定与读取都经此 module；工具经 {@link #require} 读取预加载数据，不再各自判断上下文是否存在、回退查库。
 */
public final class ChatContext {

  private static final ScopedValue<Object> CURRENT = ScopedValue.newInstance();

  private ChatContext() {}

  /** 在给定上下文内执行对话调用。 */
  public static <T, X extends Throwable> T with(Object context, ScopedValue.CallableOp<T, X> op)
      throws X {
    return ScopedValue.where(CURRENT, context).call(op);
  }

  /** 读取当前对话上下文；缺失或类型不符时抛出，说明对话入口未按约定绑定。 */
  public static <T> T require(Class<T> type) {
    Object context = CURRENT.isBound() ? CURRENT.get() : null;
    if (!type.isInstance(context)) {
      throw new IllegalStateException("缺少对话上下文：" + type.getSimpleName());
    }
    return type.cast(context);
  }

  /** 读取当前对话上下文；缺失或类型不符时返回 null（供同时服务非对话路径的调用方使用）。 */
  public static <T> @Nullable T current(Class<T> type) {
    Object context = CURRENT.isBound() ? CURRENT.get() : null;
    return type.isInstance(context) ? type.cast(context) : null;
  }
}
