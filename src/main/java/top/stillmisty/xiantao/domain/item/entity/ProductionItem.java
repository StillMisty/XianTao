package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 灵兽产物条目，从 BeastEgg 提取以降低嵌套深度 */
public record ProductionItem(
    @JsonProperty("weight") int weight,
    @JsonProperty("template_id") long templateId,
    @JsonProperty("name") String name) {}
