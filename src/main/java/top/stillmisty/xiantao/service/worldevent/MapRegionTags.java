package top.stillmisty.xiantao.service.worldevent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import top.stillmisty.xiantao.domain.map.entity.MapNode;

/**
 * 地图区域标签推断 — {@code map_node} 当前没有标签列，按地图名称关键词推断生态标签，供 {@code
 * world_event_template.valid_region_tags} 匹配使用。
 *
 * <p>标签取值与种子数据一致（forest/swamp/snow…）；后续若 {@code map_node} 增加标签列，应改为优先读取列值，本类仅作兜底推断。
 */
final class MapRegionTags {

  private record Rule(Set<String> keywords, Set<String> tags) {}

  private static final List<Rule> RULES =
      List.of(
          new Rule(Set.of("沼泽", "泥沼"), Set.of("swamp")),
          new Rule(Set.of("雪", "冰", "霜", "冻原"), Set.of("snow")),
          new Rule(Set.of("火山", "岩浆", "焚"), Set.of("volcano", "lava")),
          new Rule(Set.of("沙漠", "荒漠", "沙海"), Set.of("desert")),
          new Rule(Set.of("竹", "林", "枫"), Set.of("forest")),
          new Rule(Set.of("谷"), Set.of("valley")),
          new Rule(Set.of("潭", "泉"), Set.of("spring")),
          new Rule(Set.of("坡", "草原"), Set.of("grassland")),
          new Rule(Set.of("河", "海", "水"), Set.of("river")),
          new Rule(Set.of("遗址", "遗迹", "洞天", "废墟", "窟", "墟"), Set.of("ruins")),
          new Rule(Set.of("幽冥", "幽", "怨灵", "墓", "魔"), Set.of("graveyard")),
          new Rule(Set.of("山", "峰", "岭", "脉"), Set.of("mountain")));

  private MapRegionTags() {}

  /** 推断地图节点的区域标签（小写英文，与 valid_region_tags 种子口径一致）。 */
  static Set<String> of(MapNode node) {
    String name = node.getName();
    if (name == null || name.isBlank()) {
      return Set.of();
    }
    Set<String> tags = new HashSet<>();
    for (Rule rule : RULES) {
      if (rule.keywords().stream().anyMatch(name::contains)) {
        tags.addAll(rule.tags());
      }
    }
    if (name.contains("峰") || name.contains("不周")) {
      tags.add("peaks");
    }
    return Set.copyOf(tags);
  }
}
