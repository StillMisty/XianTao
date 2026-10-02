package top.stillmisty.xiantao.service.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.RewardGrant;
import top.stillmisty.xiantao.service.SpiritStoneService;

@Component
@RequiredArgsConstructor
public class DungeonLootHelper {

  private final ItemTemplateRepository itemTemplateRepository;
  private final RewardGrant rewardGrant;
  private final SpiritStoneService spiritStoneService;

  public record SimpleLootResult(
      List<String> descriptions, long spiritStones, Map<String, ItemTemplate> nameToTemplate) {}

  public SimpleLootResult rollAndGiveLoot(Long userId, DungeonTemplate.Poi poi) {

    List<String> descriptions = new ArrayList<>();
    Map<String, ItemTemplate> nameToTemplate = new HashMap<>();
    List<DropItem> drops = new ArrayList<>();
    long spiritStones;

    List<DungeonTemplate.LootEntry> lootPool = poi.lootPool();
    if (lootPool != null && !lootPool.isEmpty()) {
      int rollCount = 1 + ThreadLocalRandom.current().nextInt(1, 3);

      // 批量预加载掉落池模板，避免每次 roll 逐条查询
      Map<Long, ItemTemplate> lootTemplates =
          itemTemplateRepository
              .findByIds(
                  lootPool.stream().map(DungeonTemplate.LootEntry::templateId).distinct().toList())
              .stream()
              .collect(java.util.stream.Collectors.toMap(ItemTemplate::getId, t -> t));

      for (int i = 0; i < rollCount; i++) {
        DungeonTemplate.LootEntry entry =
            WeightedRandom.select(
                lootPool, DungeonTemplate.LootEntry::weight, ThreadLocalRandom.current());
        if (entry == null) continue;

        ItemTemplate template = lootTemplates.get(entry.templateId());
        String itemName = template != null ? template.getName() : "未知物品";

        int minQty = entry.minQty() != null ? entry.minQty() : 1;
        int maxQty = entry.maxQty() != null ? entry.maxQty() : 1;
        int qty = ThreadLocalRandom.current().nextInt(minQty, maxQty + 1);

        if (template != null) {
          drops.add(new DropItem(DropItem.DropType.ITEM, template.getId(), itemName, qty));
          nameToTemplate.put(itemName, template);
        }

        descriptions.add(itemName + "×" + qty);
      }
      rewardGrant.grant(userId, drops);
    }

    spiritStones = ThreadLocalRandom.current().nextInt(10, 51);
    if (spiritStones > 0) {
      spiritStoneService.deposit(userId, spiritStones, "dungeon");
    }

    return new SimpleLootResult(descriptions, spiritStones, nameToTemplate);
  }

  public long rollSpiritStones(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }
}
