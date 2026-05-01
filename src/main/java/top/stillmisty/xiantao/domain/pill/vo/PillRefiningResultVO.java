package top.stillmisty.xiantao.domain.pill.vo;

import java.util.List;
import java.util.Map;

/**
 * 炼丹结果VO
 */
public record PillRefiningResultVO(
        boolean success,
        String message,
        Long pillTemplateId,
        String pillName,
        int quantity,
        String quality,
        Map<String, Integer> usedHerbs,
        List<String> missingElements
) {
}