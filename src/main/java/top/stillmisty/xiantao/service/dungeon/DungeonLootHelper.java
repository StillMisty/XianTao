package top.stillmisty.xiantao.service.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonPoiConfig;
import top.stillmisty.xiantao.domain.dungeon.enums.PoiType;
import top.stillmisty.xiantao.domain.dungeon.vo.DropItemVO;
import top.stillmisty.xiantao.domain.dungeon.vo.ExploreResultVO;
import top.stillmisty.xiantao.domain.dungeon.vo.LootPoolEntry;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.service.inventory.StackableItemService;
import top.stillmisty.xiantao.service.player.UserStateService;

@Component
@RequiredArgsConstructor
public class DungeonLootHelper {

  private final ItemTemplateRepository itemTemplateRepository;
  private final StackableItemService stackableItemService;
  private final UserStateService userStateService;

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

    for (int i = 0; i < rollCount; i++) {
      LootPoolEntry entry =
          weightedRandom(poi.getLootPool(), LootPoolEntry::weight, poi.getLootWeightTotal());
      if (entry == null) continue;

      int qty = ThreadLocalRandom.current().nextInt(entry.minQty(), entry.maxQty() + 1);
      String itemName =
          itemTemplateRepository.findById(entry.templateId()).map(ItemTemplate::getName).orElse("未知物品");
      drops.add(new DropItemVO(itemName, qty));
    }
    return drops;
  }

  public void giveDrops(Long userId, List<DropItemVO> drops, long spiritStones) {
    for (DropItemVO drop : drops) {
      var template = itemTemplateRepository.findByName(drop.name());
        template.ifPresent(itemTemplate -> stackableItemService.addStackableItem(
                userId,
                itemTemplate.getId(),
                itemTemplate.getType(),
                itemTemplate.getName(),
                drop.quantity()
        ));
    }
    if (spiritStones > 0) {
      User user = userStateService.loadUser(userId);
      user.setSpiritStones(
          (user.getSpiritStones() != null ? user.getSpiritStones() : 0) + spiritStones);
      userStateService.save(user);
    }
  }

  private <T> T weightedRandom(
      List<T> items, java.util.function.ToIntFunction<T> weightFn, int totalWeight) {
    if (items == null || items.isEmpty() || totalWeight <= 0) return null;
    int roll = ThreadLocalRandom.current().nextInt(totalWeight);
    int cumulative = 0;
    for (T item : items) {
      cumulative += weightFn.applyAsInt(item);
      if (roll < cumulative) return item;
    }
    return items.getLast();
  }
}
