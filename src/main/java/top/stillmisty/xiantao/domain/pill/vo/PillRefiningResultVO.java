package top.stillmisty.xiantao.domain.pill.vo;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/** 炼丹结果VO */
public record PillRefiningResultVO(
    String message,
    Long pillTemplateId,
    String pillName,
    int quantity,
    String quality,
    Map<String, Integer> usedHerbs,
    @Nullable List<String> missingElements) {}
