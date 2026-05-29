package top.stillmisty.xiantao.service.dungeon;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.enums.PoiType;
import top.stillmisty.xiantao.domain.dungeon.vo.DropItemVO;
import top.stillmisty.xiantao.domain.dungeon.vo.LootPoolEntry;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Component
@RequiredArgsConstructor
public class DungeonLootHelper {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final SpiritStoneService spiritStoneService;

  public LootRollResult rollLoot(DungeonPoiConfig poi) {
    List<DropItemVO> drops = new ArrayList<>();
    if (!poi.hasLootPool()) return new LootRollResult(drops, Map.of());

    int rollCount =
        1
            + ThreadLocalRandom.current()
                .nextInt(
                    poi.getPoiType() == PoiType.BOSS ? 3 : 1,
                    poi.getPoiType() == PoiType.BOSS ? 5 : 2);

    List<LootPoolEntry> entries = new ArrayList<>();
    for (int i = 0; i < rollCount; i++) {
      LootPoolEntry entry =
          WeightedRandom.select(
              poi.getLootPool(), LootPoolEntry::weight, ThreadLocalRandom.current());
      if (entry != null) entries.add(entry);
    }
    if (entries.isEmpty()) return new LootRollResult(drops, Map.of());

    List<Long> templateIds = entries.stream().map(LootPoolEntry::templateId).distinct().toList();
    Map<Long, ItemTemplate> templateLookup =
        itemTemplateRepository.findByIds(templateIds).stream()
            .collect(Collectors.toMap(ItemTemplate::getId, t -> t));

    Map<String, ItemTemplate> nameToTemplate = new HashMap<>();
    for (LootPoolEntry entry : entries) {
      int qty = ThreadLocalRandom.current().nextInt(entry.minQty(), entry.maxQty() + 1);
      ItemTemplate template = templateLookup.get(entry.templateId());
      String itemName = template != null ? template.getName() : "未知物品";
      drops.add(new DropItemVO(itemName, qty));
      if (template != null) {
        nameToTemplate.put(itemName, template);
      }
    }
    return new LootRollResult(drops, nameToTemplate);
  }

  public void giveDrops(
      Long userId,
      List<DropItemVO> drops,
      long spiritStones,
      Map<String, ItemTemplate> nameToTemplate) {
    for (DropItemVO drop : drops) {
      ItemTemplate template = nameToTemplate.get(drop.name());
      if (template != null) {
        stackableItemService.addStackableItem(
            userId, template.getId(), template.getType(), template.getName(), drop.quantity());
      }
    }
    if (spiritStones > 0) {
      spiritStoneService.deposit(userId, spiritStones);
    }
  }

  public record LootRollResult(List<DropItemVO> drops, Map<String, ItemTemplate> nameToTemplate) {}
}
