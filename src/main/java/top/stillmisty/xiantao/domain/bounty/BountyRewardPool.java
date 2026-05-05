package top.stillmisty.xiantao.domain.bounty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  record RareItem(int weight, int minCount, int maxCount, @JsonProperty("name") String label)
      implements BountyRewardPool {
    @Override
    public String displayText() {
      if (minCount == maxCount) {
        return label + " x" + minCount;
      }
      return label + " x" + minCount + "~" + maxCount;
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  record SpiritStones(
      int weight, long minAmount, long maxAmount, @JsonProperty("name") String label)
      implements BountyRewardPool {
    @Override
    public String displayText() {
      if (minAmount == maxAmount) {
        return label + " " + minAmount;
      }
      return label + " " + minAmount + "~" + maxAmount;
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
  record EquipmentReward(int weight, long templateId, @JsonProperty("name") String label)
      implements BountyRewardPool {
    @Override
    public String displayText() {
      return label;
    }
  }
}
