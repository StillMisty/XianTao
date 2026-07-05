package top.stillmisty.xiantao.service.ai;

import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.user.entity.Player;

/** 单次秘境对话上下文，持有预加载的玩家、秘境实例、秘境模板和秘境之灵状态，避免每次 Tool 调用都重复查询。 */
public final class DungeonChatContext {

  private static final ScopedValue<DungeonChatContext> CURRENT = ScopedValue.newInstance();

  private final Player user;
  private final DungeonInstance instance;
  private final DungeonTemplate dungeon;
  @Nullable private final DungeonSpiritState spiritState;

  private DungeonChatContext(
      Player user,
      DungeonInstance instance,
      DungeonTemplate dungeon,
      @Nullable DungeonSpiritState spiritState) {
    this.user = user;
    this.instance = instance;
    this.dungeon = dungeon;
    this.spiritState = spiritState;
  }

  public static <T, X extends Throwable> T with(
      Player user,
      DungeonInstance instance,
      DungeonTemplate dungeon,
      @Nullable DungeonSpiritState spiritState,
      ScopedValue.CallableOp<T, X> op)
      throws X {
    return ScopedValue.where(CURRENT, new DungeonChatContext(user, instance, dungeon, spiritState))
        .call(op);
  }

  @Nullable
  public static DungeonChatContext current() {
    return CURRENT.isBound() ? CURRENT.get() : null;
  }

  public static boolean isSet() {
    return CURRENT.isBound();
  }

  public Player user() {
    return user;
  }

  public DungeonInstance instance() {
    return instance;
  }

  public DungeonTemplate dungeon() {
    return dungeon;
  }

  @Nullable
  public DungeonSpiritState spiritState() {
    return spiritState;
  }
}
