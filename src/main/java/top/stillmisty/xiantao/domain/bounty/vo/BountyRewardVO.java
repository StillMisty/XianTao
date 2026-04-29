package top.stillmisty.xiantao.domain.bounty.vo;

import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> items,
        Long spiritStones,
        boolean hasBeastEgg
) {
}