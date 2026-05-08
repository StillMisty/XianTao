package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 灵兽/灵种产物条目 */
public record ProductionItem(
    @JsonProperty("weight") int weight, @JsonProperty("template_id") long templateId) {}
