package top.stillmisty.xiantao.domain.dungeon.vo;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record ExploreResultVO(
    String poiName,
    String poiType,
    boolean combatOccurred,
    @Nullable String combatSummary,
    @Nullable List<DropItemVO> items,
    long expGained,
    long spiritStonesGained,
    boolean passageUnlocked,
    String message) {}
