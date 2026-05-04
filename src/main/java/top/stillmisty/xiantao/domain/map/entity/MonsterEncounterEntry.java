package top.stillmisty.xiantao.domain.map.entity;

/**
 * 遇怪条目 —— JSONB 数组元素，替代 Map&lt;Long, MonsterSpawn&gt;
 */
public record MonsterEncounterEntry(
        Long templateId,
        int weight,
        int min,
        int max
) {}
