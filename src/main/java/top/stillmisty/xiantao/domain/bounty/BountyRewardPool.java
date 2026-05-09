package top.stillmisty.xiantao.domain.bounty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.function.Function;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
    defaultImpl = BountyRewardPool.RareItem.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = BountyRewardPool.RareItem.class, name = "rare_item"),
  @JsonSubTypes.Type(value = BountyRewardPool.SpiritStones.class, name = "spirit_stones"),
  @JsonSubTypes.Type(value = BountyRewardPool.BeastEgg.class, name = "beast_egg"),
  @JsonSubTypes.Type(value = BountyRewardPool.EquipmentReward.class, name = "equipment")
})
public sealed interface BountyRewardPool {

  int weight();

  String label();

  String displayText();

  default String label(
      Function<Long, String> itemNameResolver, Function<Long, String> equipNameResolver) {
    return label();
  }

  default String displayText(
      Function<Long, String> itemNameResolver, Function<Long, String> equipNameResolver) {
    return displayText();
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  record RareItem(
      int weight,
      @JsonProperty("min") int minCount,
      @JsonProperty("max") int maxCount,
      @JsonProperty("template_id") long templateId)
      implements BountyRewardPool {
    @Override
    public String label() {
      return "稀有物品";
    }

    @Override
    public String displayText() {
      if (minCount == maxCount) {
        return "稀有物品 x" + minCount;
      }
      return "稀有物品 x" + minCount + "~" + maxCount;
    }

    @Override
    public String label(
        Function<Long, String> itemNameResolver, Function<Long, String> equipNameResolver) {
      return itemNameResolver.apply(templateId);
    }

    @Override
    public String displayText(
        Function<Long, String> itemNameResolver, Function<Long, String> equipNameResolver) {
      String name = itemNameResolver.apply(templateId);
      if (minCount == maxCount) {
        return name + " x" + minCount;
      }
      return name + " x" + minCount + "~" + maxCount;
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  record SpiritStones(
      int weight, @JsonProperty("min") long minAmount, @JsonProperty("max") long maxAmount)
      implements BountyRewardPool {
    @Override
    public String label() {
      return "灵石";
    }

    @Override
    public String displayText() {
      if (minAmount == maxAmount) {
        return "灵石 " + minAmount;
      }
      return "灵石 " + minAmount + "~" + maxAmount;
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  record BeastEgg(int weight, @JsonProperty("name") String label) implements BountyRewardPool {
    @Override
    public String displayText() {
      return label;
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  record EquipmentReward(int weight, @JsonProperty("template_id") long templateId)
      implements BountyRewardPool {
    @Override
    public String label() {
      return "装备";
    }

    @Override
    public String displayText() {
      return "装备";
    }

    @Override
    public String label(
        Function<Long, String> itemNameResolver, Function<Long, String> equipNameResolver) {
      return equipNameResolver.apply(templateId);
    }

    @Override
    public String displayText(
        Function<Long, String> itemNameResolver, Function<Long, String> equipNameResolver) {
      return equipNameResolver.apply(templateId);
    }
  }
}
