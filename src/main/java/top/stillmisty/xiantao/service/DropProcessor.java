package top.stillmisty.xiantao.service;

import java.util.ArrayList;
import java.util.Comparator;
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

    List<DropItem> candidates = new ArrayList<>();

    for (var entry : equipmentDrops) {
      if (ThreadLocalRandom.current().nextInt(100)
          < Math.max(1, (int) (entry.weight() * wealthMultiplier))) {
        EquipmentTemplate tmplEquip = equipTmplMap.get(entry.templateId());
        if (tmplEquip != null) {
          candidates.add(
              new DropItem(DropType.EQUIPMENT, entry.templateId(), tmplEquip.getName(), 1));
        }
      }
    }

    for (var entry : itemDrops) {
      if (ThreadLocalRandom.current().nextInt(100)
          < Math.max(1, (int) (entry.weight() * wealthMultiplier))) {
        ItemTemplate tmplItem = itemTmplMap.get(entry.templateId());
        if (tmplItem != null) {
          int qty = 1 + ThreadLocalRandom.current().nextInt(3);
          candidates.add(new DropItem(DropType.ITEM, entry.templateId(), tmplItem.getName(), qty));
        }
      }
    }

    int maxDrops = 2 + ThreadLocalRandom.current().nextInt(2);
    candidates.sort(
        Comparator.comparingInt(
            a -> {
              var entry =
                  dropTable.stream().filter(d -> d.templateId().equals(a.templateId())).findFirst();
              return entry.map(e -> -e.weight()).orElse(0);
            }));
    for (int i = 0; i < Math.min(maxDrops, candidates.size()); i++) {
      drops.add(candidates.get(i));
    }

    return drops;
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
