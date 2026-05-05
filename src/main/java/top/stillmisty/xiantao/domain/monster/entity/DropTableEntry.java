package top.stillmisty.xiantao.domain.monster.entity;

/** 掉落表条目 —— JSONB 数组元素，替代 Map&lt;String, Object&gt; */
public record DropTableEntry(String category, Long templateId, int weight) {}
