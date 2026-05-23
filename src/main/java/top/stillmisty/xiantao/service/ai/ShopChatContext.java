package top.stillmisty.xiantao.service.ai;

import java.util.List;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;

/** 单次店铺对话上下文，持有预加载的玩家、掌柜和世界事件，避免每次 Tool 调用都重复查询。 */
public final class ShopChatContext {

  private static final ScopedValue<ShopChatContext> CURRENT = ScopedValue.newInstance();

  private final User user;
  private final ShopNpc npc;
  private final List<WorldEvent> activeEvents;
  private boolean haggleUsed;

  private ShopChatContext(User user, ShopNpc npc, List<WorldEvent> activeEvents) {
    this.user = user;
    this.npc = npc;
    this.activeEvents = activeEvents;
  }

  public static <T, X extends Throwable> T with(
      User user, ShopNpc npc, List<WorldEvent> activeEvents, ScopedValue.CallableOp<T, X> op)
      throws X {
    return ScopedValue.where(CURRENT, new ShopChatContext(user, npc, activeEvents)).call(op);
  }

  public static ShopChatContext current() {
    return CURRENT.isBound() ? CURRENT.get() : null;
  }

  public static boolean isSet() {
    return CURRENT.isBound();
  }

  public User user() {
    return user;
  }

  public ShopNpc npc() {
    return npc;
  }

  public List<WorldEvent> activeEvents() {
    return activeEvents;
  }

  public boolean isHaggleUsed() {
    return haggleUsed;
  }

  public void markHaggled() {
    this.haggleUsed = true;
  }
}
