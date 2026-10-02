package top.stillmisty.xiantao.service.ai;

import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonInstance;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonSpiritState;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.user.entity.Player;

/** 单次秘境对话的预加载数据：玩家、秘境实例、秘境模板和秘境之灵状态，避免每次 Tool 调用都重复查询。 */
public record DungeonChatContext(
    Player user,
    DungeonInstance instance,
    DungeonTemplate dungeon,
    @Nullable DungeonSpiritState spiritState) {}
