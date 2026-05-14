package top.stillmisty.xiantao.domain.dungeon.vo;

import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonElementType;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;

public record DungeonListVO(
    Long dungeonId,
    String name,
    DungeonElementType elementType,
    int minLevel,
    int maxLevel,
    int maxTeamSize,
    boolean hasActiveInstance,
    DungeonStatus activeStatus,
    DungeonArea activeArea,
    int rewardCount,
    int dailyLimit,
    boolean firstClear) {}
