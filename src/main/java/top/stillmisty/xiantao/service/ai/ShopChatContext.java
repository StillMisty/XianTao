package top.stillmisty.xiantao.service.ai;

import java.util.List;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;

/** 单次店铺对话的预加载数据：玩家、掌柜和世界事件，避免每次 Tool 调用都重复查询。 */
public final class ShopChatContext {

  private final Player user;
  private final ShopNpc npc;
  private final List<WorldEvent> activeEvents;
  private boolean haggleUsed;

  public ShopChatContext(Player user, ShopNpc npc, List<WorldEvent> activeEvents) {
    this.user = user;
    this.npc = npc;
    this.activeEvents = activeEvents;
  }

  public Player user() {
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
