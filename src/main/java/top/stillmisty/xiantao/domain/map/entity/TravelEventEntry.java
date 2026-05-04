package top.stillmisty.xiantao.domain.map.entity;

/**
 * 旅行事件条目 —— JSONB 数组元素，替代 Map&lt;String, Integer&gt;
 */
public record TravelEventEntry(
        String eventType,
        int weight
) {}
