package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.monster.entity.DropTableEntry;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.monster.vo.DropItem.DropType;
import top.stillmisty.xiantao.infrastructure.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.inventory.EquipmentService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

@Slf4j
@Component
@RequiredArgsConstructor
public class DropProcessor {

  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentTemplateRepository equipmentTemplateRepository;
  private final EquipmentService equipmentService;
  private final StackableItemService stackableItemService;
  private final FortuneService fortuneService;

  /** 掉落模型：每条掉落表项独立掷骰，weight 即基础掉率百分比（0-100）； 财富加成作用于物品数量而非概率；不做按权重排序截断，避免稀有掉落被高权重条目结构性挤出。 */
  public List<DropItem> processMonsterDrops(MonsterTemplate tmpl, Long userId) {
    var fortune = fortuneService.calculate(userId);
    double wealthMultiplier = fortuneService.getWealthMultiplier(fortune.wealth());
    List<DropItem> drops = new ArrayList<>();
    List<DropTableEntry> dropTable = tmpl.getDropTable();
    if (dropTable == null || dropTable.isEmpty()) return drops;

    List<DropTableEntry> equipmentDrops =
        dropTable.stream().filter(d -> "equipment".equals(d.category())).toList();
    List<DropTableEntry> itemDrops =
        dropTable.stream().filter(d -> "items".equals(d.category())).toList();

    Map<Long, EquipmentTemplate> equipTmplMap = loadEquipmentTemplates(equipmentDrops);
    Map<Long, ItemTemplate> itemTmplMap = loadItemTemplates(itemDrops);

    for (var entry : equipmentDrops) {
      if (!roll(entry.weight())) continue;
      EquipmentTemplate tmplEquip = equipTmplMap.get(entry.templateId());
      if (tmplEquip != null) {
        drops.add(new DropItem(DropType.EQUIPMENT, entry.templateId(), tmplEquip.getName(), 1));
      }
    }

    for (var entry : itemDrops) {
      if (!roll(entry.weight())) continue;
      ItemTemplate tmplItem = itemTmplMap.get(entry.templateId());
      if (tmplItem != null) {
        int baseQty = 1 + ThreadLocalRandom.current().nextInt(3);
        int qty = (int) Math.max(1, Math.round(baseQty * Math.max(1.0, wealthMultiplier)));
        drops.add(new DropItem(DropType.ITEM, entry.templateId(), tmplItem.getName(), qty));
      }
    }

    // 安全上限：超限时随机保留而非按权重保留
    int maxDrops = 5;
    if (drops.size() > maxDrops) {
      java.util.Collections.shuffle(drops);
      return new ArrayList<>(drops.subList(0, maxDrops));
    }
    return drops;
  }

  /** 独立掉率判定 */
  private static boolean roll(double weightPercent) {
    if (weightPercent >= 100) return true;
    if (weightPercent <= 0) return false;
    return ThreadLocalRandom.current().nextDouble(100) < weightPercent;
  }

  @Transactional
  public void distributeDrops(Long userId, List<DropItem> drops) {
    List<Long> itemTemplateIds =
        drops.stream()
            .filter(d -> d.type() != DropType.EQUIPMENT)
            .map(DropItem::templateId)
            .distinct()
            .toList();
    Map<Long, ItemTemplate> templateMap =
        itemTemplateIds.isEmpty()
            ? Map.of()
            : itemTemplateRepository.findByIds(itemTemplateIds).stream()
                .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    for (DropItem drop : drops) {
      if (drop.type() == DropType.EQUIPMENT) {
        equipmentService.createEquipment(userId, drop.templateId());
      } else {
        ItemTemplate tmpl = templateMap.get(drop.templateId());
        ItemType type = tmpl != null ? tmpl.getType() : ItemType.MATERIAL;
        stackableItemService.addStackableItem(
            userId, drop.templateId(), type, drop.name(), drop.quantity());
      }
    }
  }

  private Map<Long, EquipmentTemplate> loadEquipmentTemplates(List<DropTableEntry> equipmentDrops) {
    List<Long> ids = extractTemplateIds(equipmentDrops);
    if (ids.isEmpty()) return Map.of();
    return equipmentTemplateRepository.findByIds(ids).stream()
        .collect(Collectors.toMap(EquipmentTemplate::getId, t -> t));
  }

  private Map<Long, ItemTemplate> loadItemTemplates(List<DropTableEntry> itemDrops) {
    List<Long> ids = extractTemplateIds(itemDrops);
    if (ids.isEmpty()) return Map.of();
    return itemTemplateRepository.findByIds(ids).stream()
        .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
  }

  private List<Long> extractTemplateIds(List<DropTableEntry> drops) {
    if (drops == null || drops.isEmpty()) return List.of();
    return drops.stream().map(DropTableEntry::templateId).distinct().toList();
  }
}
