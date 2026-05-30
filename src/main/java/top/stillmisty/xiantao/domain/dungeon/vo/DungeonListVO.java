package top.stillmisty.xiantao.domain.dungeon.vo;

public record DungeonListVO(
    Long dungeonId,
    String name,
    String description,
    String elementName,
    int minLevel,
    int maxLevel,
    int maxTeamSize,
    boolean hasActiveInstance,
    int rewardCount,
    int dailyLimit,
    boolean firstClear) {}
