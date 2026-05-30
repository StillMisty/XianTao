package top.stillmisty.xiantao.domain.bounty.vo;

import java.time.LocalDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;

public record BountyStatusVO(
    @Nullable Long bountyId,
    String bountyName,
    String description,
    @Nullable LocalDateTime startTime,
    int durationMinutes,
    long minutesElapsed,
    long minutesRemaining,
    List<BountyRewardItem> rewards) {}
