package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

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

    record Stat(@JsonProperty("stat_attr") String statAttr, int amount) implements Effect {}

    record Breakthrough(double rate) implements Effect {}

    record Buff(String attribute, int amount, @JsonProperty("duration_seconds") int durationSeconds)
        implements Effect {}

    record Cure(String status) implements Effect {}
  }

  record Growth(
      @JsonProperty("grow_time") int growTime,
      @JsonProperty("reharvest") int reharvest,
      @JsonProperty("production_items") List<ProductionItem> productionItems)
      implements ItemProperties {
    public Growth {
      if (reharvest < 0) reharvest = 0;
      if (productionItems == null) productionItems = List.of();
    }
  }

  record BeastEgg(
      @JsonProperty("grow_time") int growTime,
      @JsonProperty("production_items") List<ProductionItem> productionItems,
      @JsonProperty("skill_pool") BeastSkillPool skillPool)
      implements ItemProperties {}

  record SkillJade(@JsonProperty("skill_id") long skillId) implements ItemProperties {}

  record Potion(@JsonProperty("effects") List<Effect> effects) implements ItemProperties {}

  record Scroll(int grade, RecipeProduct product, List<ElementRequirement> requirements)
      implements ItemProperties {}
}
