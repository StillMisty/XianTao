package top.stillmisty.xiantao.domain.dungeon.vo;

import java.util.List;

public record ExploreResultVO(
    String poiName,
    String poiType,
    boolean combatOccurred,
    String combatSummary,
    List<DropItemVO> items,
    long expGained,
    long spiritStonesGained,
    boolean passageUnlocked,
    String message) {}
