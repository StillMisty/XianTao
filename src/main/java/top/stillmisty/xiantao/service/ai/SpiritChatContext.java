package top.stillmisty.xiantao.service.ai;

import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;

/** 单次地灵对话上下文，持有预加载的福地和地灵数据，避免每次 Tool 调用都重复查询。 */
public final class SpiritChatContext {

  private static final ScopedValue<SpiritChatContext> CURRENT = ScopedValue.newInstance();

  private final Fudi fudi;
  private final Spirit spirit;

  private SpiritChatContext(Fudi fudi, Spirit spirit) {
    this.fudi = fudi;
    this.spirit = spirit;
  }

  public static <T, X extends Throwable> T with(
      Fudi fudi, Spirit spirit, ScopedValue.CallableOp<T, X> op) throws X {
    return ScopedValue.where(CURRENT, new SpiritChatContext(fudi, spirit)).call(op);
  }

  @Nullable
  public static SpiritChatContext current() {
    return CURRENT.isBound() ? CURRENT.get() : null;
  }

  public static boolean isSet() {
    return CURRENT.isBound();
  }

  public Fudi fudi() {
    return fudi;
  }

  public Spirit spirit() {
    return spirit;
  }
}
