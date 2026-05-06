package top.stillmisty.xiantao.domain.bounty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 预存的悬赏奖励物品 — 接取时确定，完成时发放 */
public sealed interface BountyRewardItem {

  record ItemReward(Long templateId, String name, int quantity) implements BountyRewardItem {}

  record SpiritStonesReward(long amount) implements BountyRewardItem {}

  record BeastEggReward(Long templateId, String name) implements BountyRewardItem {}

  record EquipmentRewardItem(Long templateId, String name) implements BountyRewardItem {}

  /** 序列化为 Map（用于存入 JSONB） */
  default Map<String, Object> toMap() {
    return switch (this) {
      case ItemReward(var templateId, var name, var quantity) -> {
        Map<String, Object> m = new HashMap<>();
        m.put("templateId", templateId);
        m.put("name", name);
        m.put("quantity", quantity);
        yield m;
      }
      case SpiritStonesReward(var amount) -> {
        Map<String, Object> m = new HashMap<>();
        m.put("_rewardType", "spirit_stones");
        m.put("amount", amount);
        yield m;
      }
      case BeastEggReward(var templateId, var name) -> {
        Map<String, Object> m = new HashMap<>();
        m.put("_rewardType", "BEAST_EGG");
        m.put("name", name);
        m.put("templateId", templateId);
        m.put("quantity", 1);
        yield m;
      }
      case EquipmentRewardItem(var templateId, var name) -> {
        Map<String, Object> m = new HashMap<>();
        m.put("_rewardType", "equipment");
        m.put("name", name);
        m.put("templateId", templateId);
        m.put("quantity", 1);
        yield m;
      }
    };
  }

  static List<BountyRewardItem> parse(List<Map<String, Object>> raw) {
    if (raw == null) return List.of();
    return raw.stream().map(BountyRewardItem::parseOne).toList();
  }

  public static BountyRewardItem parseOne(Map<String, Object> map) {
    String rewardType = (String) map.get("_rewardType");
    if ("spirit_stones".equals(rewardType)) {
      return new SpiritStonesReward(((Number) map.getOrDefault("amount", 0)).longValue());
    }
    if ("BEAST_EGG".equals(rewardType)) {
      return new BeastEggReward(toLong(map.get("templateId")), (String) map.get("name"));
    }
    if ("equipment".equals(rewardType)) {
      return new EquipmentRewardItem(toLong(map.get("templateId")), (String) map.get("name"));
    }
    return new ItemReward(
        toLong(map.get("templateId")),
        (String) map.get("name"),
        ((Number) map.getOrDefault("quantity", 1)).intValue());
  }

  private static Long toLong(Object value) {
    if (value instanceof Long longVal) return longVal;
    if (value instanceof Number number) return number.longValue();
    return null;
  }
}
