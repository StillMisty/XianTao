package top.stillmisty.xiantao.domain.bounty;

import java.util.List;
import java.util.Map;

/**
 * 悬赏奖池项 — 从 JSONB rewards 解析后的类型安全表示
 */
public sealed interface BountyRewardPool {

    int weight();

    record RareItem(int weight, int count) implements BountyRewardPool {}

    record SpiritStones(int weight, long amount) implements BountyRewardPool {}

    record BeastEgg(int weight) implements BountyRewardPool {}

    static List<BountyRewardPool> parse(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .map(BountyRewardPool::parseOne)
                .toList();
    }

    private static BountyRewardPool parseOne(Map<String, Object> map) {
        int weight = ((Number) map.getOrDefault("weight", 1)).intValue();
        String type = (String) map.get("type");
        if (type == null) return new RareItem(weight, 1);
        return switch (type) {
            case "rare_item" -> new RareItem(weight, ((Number) map.getOrDefault("count", 1)).intValue());
            case "spirit_stones" -> new SpiritStones(weight, ((Number) map.getOrDefault("amount", 0)).longValue());
            case "beast_egg" -> new BeastEgg(weight);
            default -> new RareItem(weight, 1);
        };
    }
}
