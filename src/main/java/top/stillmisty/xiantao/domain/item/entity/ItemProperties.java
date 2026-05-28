package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.Map;

/** 物品属性密封接口，按 ItemType 路由到类型安全子类 */
public sealed interface ItemProperties {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = Effect.Exp.class, name = "exp"),
    @JsonSubTypes.Type(value = Effect.Hp.class, name = "hp"),
    @JsonSubTypes.Type(value = Effect.Stat.class, name = "stat"),
    @JsonSubTypes.Type(value = Effect.Breakthrough.class, name = "breakthrough"),
    @JsonSubTypes.Type(value = Effect.Buff.class, name = "buff"),
    @JsonSubTypes.Type(value = Effect.Cure.class, name = "cure"),
  })
  sealed interface Effect {
    record Exp(long amount) implements Effect {}

    record Hp(long amount, double percentage) implements Effect {}

    record Stat(@JsonProperty("statAttr") String statAttr, int amount) implements Effect {}

    record Breakthrough(double rate) implements Effect {}

    record Buff(String attribute, int amount, @JsonProperty("duration_seconds") int durationSeconds)
        implements Effect {}

    record Cure(String status) implements Effect {}
  }

  record Growth(
      @JsonProperty("grow_time") int growTime,
      @JsonProperty("max_harvest") int maxHarvest,
      @JsonProperty("yield_min") int yieldMin,
      @JsonProperty("yield_max") int yieldMax,
      @JsonProperty("mutation") Mutation mutation,
      @JsonProperty("production_items") List<SeedProduct> productionItems)
      implements ItemProperties {
    public Growth {
      if (maxHarvest <= 0) maxHarvest = 1;
      if (yieldMin <= 0) yieldMin = 1;
      if (yieldMax <= 0) yieldMax = 3;
      if (productionItems == null) productionItems = List.of();
    }
  }

  record Mutation(
      @JsonProperty("chance") double chance, @JsonProperty("template_id") long templateId) {}

  record BeastEgg(@JsonProperty("beast_template_id") long beastTemplateId)
      implements ItemProperties {}

  record SkillJade(@JsonProperty("skill_id") long skillId) implements ItemProperties {}

  record Potion(@JsonProperty("effects") List<Effect> effects) implements ItemProperties {}

  record ForgingBlueprint(
      @JsonProperty("equipment_template_id") long equipmentTemplateId,
      int grade,
      Map<String, ElementRange> requirements)
      implements ItemProperties {}

  record Scroll(@JsonProperty("recipe") Recipe recipe) implements ItemProperties {
    public int grade() {
      return recipe.grade();
    }

    public long resultItemId() {
      return recipe.resultItemId();
    }

    public int resultQuantity() {
      return recipe.resultQuantity();
    }

    public Map<String, ElementRange> requirements() {
      return recipe.requirements();
    }

    public record Recipe(
        int grade,
        @JsonProperty("result_item_id") long resultItemId,
        @JsonProperty("result_quantity") int resultQuantity,
        Map<String, ElementRange> requirements) {}
  }

  record Herb(@JsonProperty("elements") Map<String, Integer> elements) implements ItemProperties {}

  record Material(
      @JsonProperty("RIGIDITY") int rigidity,
      @JsonProperty("TOUGHNESS") int toughness,
      @JsonProperty("SPIRIT") int spirit)
      implements ItemProperties {}
}
