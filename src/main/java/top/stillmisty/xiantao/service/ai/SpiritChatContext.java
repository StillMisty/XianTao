package top.stillmisty.xiantao.service.ai;

import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;

/** 单次地灵对话上下文，持有预加载的福地、地灵和进行中事件，避免每次 Tool 调用都重复查询。 */
public final class SpiritChatContext {

  private static final ScopedValue<SpiritChatContext> CURRENT = ScopedValue.newInstance();

  private final Fudi fudi;
  private final Spirit spirit;
  private final List<WorldEvent> activeEvents;

  private SpiritChatContext(Fudi fudi, Spirit spirit, List<WorldEvent> activeEvents) {
    this.fudi = fudi;
    this.spirit = spirit;
    this.activeEvents = activeEvents;
  }

  public static <T, X extends Throwable> T with(
      Fudi fudi, Spirit spirit, List<WorldEvent> activeEvents, ScopedValue.CallableOp<T, X> op)
      throws X {
    return ScopedValue.where(CURRENT, new SpiritChatContext(fudi, spirit, activeEvents)).call(op);
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

  /** 当前玩家位置可见的进行中世界事件（全局 + 本地区域）。 */
  public List<WorldEvent> activeEvents() {
    return activeEvents;
  }
}
