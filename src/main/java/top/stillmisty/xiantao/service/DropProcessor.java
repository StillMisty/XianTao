package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.monster.entity.DropTableEntry;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.monster.vo.DropItem.DropType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DropProcessor {

    private final ItemTemplateRepository itemTemplateRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final EquipmentService equipmentService;
    private final StackableItemService stackableItemService;

    public List<DropItem> processMonsterDrops(MonsterTemplate tmpl) {
        List<DropItem> drops = new ArrayList<>();
        List<DropTableEntry> dropTable = tmpl.getDropTable();
        if (dropTable == null || dropTable.isEmpty()) return drops;

        List<DropTableEntry> equipmentDrops = dropTable.stream()
                .filter(d -> "equipment".equals(d.category()))
                .toList();
        List<DropTableEntry> itemDrops = dropTable.stream()
                .filter(d -> "items".equals(d.category()))
                .toList();

        Map<Long, EquipmentTemplate> equipTmplMap = loadEquipmentTemplates(equipmentDrops);
        Map<Long, ItemTemplate> itemTmplMap = loadItemTemplates(itemDrops);

        for (var entry : equipmentDrops) {
            if (ThreadLocalRandom.current().nextInt(100) < entry.weight()) {
                EquipmentTemplate tmplEquip = equipTmplMap.get(entry.templateId());
                if (tmplEquip != null) {
                    drops.add(new DropItem(DropType.EQUIPMENT, entry.templateId(), tmplEquip.getName(), 1));
                }
            }
        }

        for (var entry : itemDrops) {
            if (ThreadLocalRandom.current().nextInt(100) < entry.weight()) {
                ItemTemplate tmplItem = itemTmplMap.get(entry.templateId());
                if (tmplItem != null) {
                    int qty = 1 + ThreadLocalRandom.current().nextInt(3);
                    drops.add(new DropItem(DropType.ITEM, entry.templateId(), tmplItem.getName(), qty));
                }
            }
        }

        return drops;
    }

    public void distributeDrops(Long userId, List<DropItem> drops) {
        for (DropItem drop : drops) {
            if (drop.type() == DropType.EQUIPMENT) {
                equipmentService.createEquipment(userId, drop.templateId());
            } else {
                stackableItemService.addStackableItem(
                        userId, drop.templateId(), ItemType.MATERIAL, drop.name(), drop.quantity());
            }
        }
    }

    private Map<Long, EquipmentTemplate> loadEquipmentTemplates(List<DropTableEntry> equipmentDrops) {
        if (equipmentDrops == null || equipmentDrops.isEmpty()) return Map.of();
        List<Long> ids = equipmentDrops.stream().map(DropTableEntry::templateId).distinct().toList();
        return equipmentTemplateRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(EquipmentTemplate::getId, t -> t));
    }

    private Map<Long, ItemTemplate> loadItemTemplates(List<DropTableEntry> itemDrops) {
        if (itemDrops == null || itemDrops.isEmpty()) return Map.of();
        List<Long> ids = itemDrops.stream().map(DropTableEntry::templateId).distinct().toList();
        return itemTemplateRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    }
}
