package top.stillmisty.xiantao.domain.map.entity;

/**
 * 相邻地图条目 —— JSONB 数组元素，替代 Map&lt;String, Integer&gt;
 */
public record NeighborEntry(
        Long targetId,
        int cost
) {}
