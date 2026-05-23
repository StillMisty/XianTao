package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 地块升级操作结果。
 *
 * <p>升级消耗灵石，等级越高产量越大。每次升级提升 1 级。
 */
public record UpgradeCellResponse(
    @JsonPropertyDescription("被升级的地块编号") String position,
    @JsonPropertyDescription("升级前等级") int oldLevel,
    @JsonPropertyDescription("升级后等级") int newLevel) {}
