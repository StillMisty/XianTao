package top.stillmisty.xiantao.domain.bounty.vo;

import lombok.Builder;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;

import java.util.List;

/**
 * 悬赏列表 VO
 */
@Builder
public record BountyVO(
        Long id,
        String name,
        String description,
        int durationMinutes,
        List<BountyRewardPool> rewards,
        int requireLevel,
        int eventWeight
) {
}
