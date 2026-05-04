package top.stillmisty.xiantao.domain.bounty.vo;

import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;

import java.time.LocalDateTime;
import java.util.List;

public record BountyStatusVO(
        Long bountyId,
        String bountyName,
        String description,
        LocalDateTime startTime,
        int durationMinutes,
        long minutesElapsed,
        long minutesRemaining,
        List<BountyRewardItem> rewards
) {
}
