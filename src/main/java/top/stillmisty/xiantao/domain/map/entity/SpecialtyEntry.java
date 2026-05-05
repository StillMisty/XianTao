package top.stillmisty.xiantao.domain.map.entity;

/** 特产条目 —— JSONB 数组元素，替代 Map&lt;Long, Integer&gt; */
public record SpecialtyEntry(Long templateId, int weight) {}
