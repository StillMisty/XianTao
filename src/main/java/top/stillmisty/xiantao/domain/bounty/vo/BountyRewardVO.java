package top.stillmisty.xiantao.domain.bounty.vo;

import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;

import java.util.List;

/**
 * 悬赏完成结果 VO
 */
public record BountyRewardVO(
        Long userId,
        Long bountyId,
        String bountyName,
        String mapName,
        long durationMinutes,
        String rewardDescription,
        String eventDescription,
        List<BountyRewardItem> items,
        Long spiritStones,
        boolean hasBeastEgg,
        boolean hasEquipment
) {
}
