package top.stillmisty.xiantao.domain.dungeon.vo;

import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;

public record DungeonListVO(
    Long dungeonId,
    String name,
    int minLevel,
    int maxLevel,
    int maxTeamSize,
    boolean hasActiveInstance,
    @Nullable DungeonStatus activeStatus,
    @Nullable DungeonArea activeArea,
    int rewardCount,
    int dailyLimit,
    boolean firstClear) {}
