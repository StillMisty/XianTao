package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 种子产出物条目 */
public record SeedProduct(@JsonProperty("template_id") long templateId) {}
