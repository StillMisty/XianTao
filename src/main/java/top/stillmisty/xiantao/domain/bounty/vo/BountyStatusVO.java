package top.stillmisty.xiantao.domain.bounty.vo;

import java.time.LocalDateTime;
import java.util.List;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;

public record BountyStatusVO(
    Long bountyId,
    String bountyName,
    String description,
    LocalDateTime startTime,
    int durationMinutes,
    long minutesElapsed,
    long minutesRemaining,
    List<BountyRewardItem> rewards) {}
