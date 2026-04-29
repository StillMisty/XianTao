package top.stillmisty.xiantao.domain.bounty.vo;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 悬赏列表 VO
 */
@Builder
public record BountyVO(
        Long id,
        String name,
        String description,
        int durationMinutes,
        List<Map<String, Object>> rewards,
        int requireLevel,
        int eventWeight
) {
}