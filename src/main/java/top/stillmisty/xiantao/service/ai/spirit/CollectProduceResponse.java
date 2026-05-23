package top.stillmisty.xiantao.service.ai.spirit;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 收获产出操作结果。
 *
 * <p>收取灵田成熟作物或兽栏灵兽产出。传 "all" 可一次性收取所有可收获地块。 单地块收取时：灵田返回 harvestCount=1，兽栏返回 collectedCount=1。
 */
public record CollectProduceResponse(
    @JsonPropertyDescription("收取的地块编号，或 'all' 表示全部") String position,
    @JsonPropertyDescription("收获的灵田数量（传 'all' 时为合计）") int harvested,
    @JsonPropertyDescription("收取的兽栏数量（传 'all' 时为合计）") int collected,
    @JsonPropertyDescription("总产出物品件数") int totalItems) {}
