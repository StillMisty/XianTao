package top.stillmisty.xiantao.service.dungeon;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.enums.PoiType;
import top.stillmisty.xiantao.domain.dungeon.vo.DropItemVO;
import top.stillmisty.xiantao.domain.dungeon.vo.ExploreResultVO;
import top.stillmisty.xiantao.domain.dungeon.vo.LootPoolEntry;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.infrastructure.util.WeightedRandom;
import top.stillmisty.xiantao.service.SpiritStoneService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Component
@RequiredArgsConstructor
public class DungeonLootHelper {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final SpiritStoneService spiritStoneService;

  @Transactional
  public ExploreResultVO executeGather(User user, DungeonPoiConfig poi) {
    List<DropItemVO> drops = rollLoot(poi);
    long spiritStones = ThreadLocalRandom.current().nextInt(10, 31);

    giveDrops(user.getId(), drops, spiritStones);

    return new ExploreResultVO(
        poi.getName(),
        "采集",
        false,
        null,
        drops,
        0,
        spiritStones,
        false,
        "你在" + poi.getName() + "中采集到了一些物资。");
  }

  @Transactional
  public ExploreResultVO executeSearch(User user, DungeonPoiConfig poi) {
    List<DropItemVO> drops = rollLoot(poi);
    long spiritStones = ThreadLocalRandom.current().nextInt(20, 81);

    boolean triggerCombat = ThreadLocalRandom.current().nextDouble() < 0.2;
    String combatSummary = null;
    if (triggerCombat) {
      combatSummary = "搜索时遭遇了守护残魂，但被你轻松解决了。";
      spiritStones += ThreadLocalRandom.current().nextInt(20, 61);
    }

    giveDrops(user.getId(), drops, spiritStones);

    return new ExploreResultVO(
        poi.getName(),
        "搜索",
        triggerCombat,
        combatSummary,
        drops,
        0,
        spiritStones,
        false,
        "你在" + poi.getName() + "中仔细搜索了一番。");
  }

  public List<DropItemVO> rollLoot(DungeonPoiConfig poi) {
    List<DropItemVO> drops = new ArrayList<>();
    if (!poi.hasLootPool()) return drops;

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
    if (entries.isEmpty()) return drops;

    List<Long> templateIds = entries.stream().map(LootPoolEntry::templateId).distinct().toList();
    Map<Long, String> nameLookup =
        itemTemplateRepository.findByIds(templateIds).stream()
            .collect(Collectors.toMap(ItemTemplate::getId, ItemTemplate::getName));

    for (LootPoolEntry entry : entries) {
      int qty = ThreadLocalRandom.current().nextInt(entry.minQty(), entry.maxQty() + 1);
      String itemName = nameLookup.getOrDefault(entry.templateId(), "未知物品");
      drops.add(new DropItemVO(itemName, qty));
    }
    return drops;
  }

  public void giveDrops(Long userId, List<DropItemVO> drops, long spiritStones) {
    for (DropItemVO drop : drops) {
      itemTemplateRepository
          .findByName(drop.name())
          .ifPresent(
              template ->
                  stackableItemService.addStackableItem(
                      userId,
                      template.getId(),
                      template.getType(),
                      template.getName(),
                      drop.quantity()));
    }
    if (spiritStones > 0) {
      spiritStoneService.deposit(userId, spiritStones);
    }
  }
}
