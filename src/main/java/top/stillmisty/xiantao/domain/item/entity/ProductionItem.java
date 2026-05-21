package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 灵兽/兽卵产出物条目 */
public record ProductionItem(
    @JsonProperty("weight") int weight, @JsonProperty("template_id") long templateId) {
  public ProductionItem {
    if (weight <= 0) weight = 1;
  }
}
