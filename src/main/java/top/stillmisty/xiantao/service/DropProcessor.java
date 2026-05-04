package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.EquipmentTemplate;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.EquipmentTemplateRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
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
        Map<String, Object> dropTable = tmpl.getDropTable();
        if (dropTable == null || dropTable.isEmpty()) return drops;

        @SuppressWarnings("unchecked")
        Map<String, Object> equipmentDrops = (Map<String, Object>) dropTable.get("equipment");
        @SuppressWarnings("unchecked")
        Map<String, Object> itemDrops = (Map<String, Object>) dropTable.get("items");

        Map<Long, EquipmentTemplate> equipTmplMap = loadEquipmentTemplates(equipmentDrops);
        Map<Long, ItemTemplate> itemTmplMap = loadItemTemplates(itemDrops);

        if (equipmentDrops != null) {
            for (var entry : equipmentDrops.entrySet()) {
                Long templateId = Long.parseLong(entry.getKey());
                int weight = ((Number) entry.getValue()).intValue();
                if (ThreadLocalRandom.current().nextInt(100) < weight) {
                    EquipmentTemplate tmplEquip = equipTmplMap.get(templateId);
                    if (tmplEquip != null) {
                        drops.add(new DropItem(DropType.EQUIPMENT, templateId, tmplEquip.getName(), 1));
                    }
                }
            }
        }

        if (itemDrops != null) {
            for (var entry : itemDrops.entrySet()) {
                Long templateId = Long.parseLong(entry.getKey());
                int weight = ((Number) entry.getValue()).intValue();
                if (ThreadLocalRandom.current().nextInt(100) < weight) {
                    ItemTemplate tmplItem = itemTmplMap.get(templateId);
                    if (tmplItem != null) {
                        int qty = 1 + ThreadLocalRandom.current().nextInt(3);
                        drops.add(new DropItem(DropType.ITEM, templateId, tmplItem.getName(), qty));
                    }
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

    private Map<Long, EquipmentTemplate> loadEquipmentTemplates(Map<String, Object> equipmentDrops) {
        if (equipmentDrops == null || equipmentDrops.isEmpty()) return Map.of();
        List<Long> ids = equipmentDrops.keySet().stream().map(Long::parseLong).toList();
        return equipmentTemplateRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(EquipmentTemplate::getId, t -> t));
    }

    private Map<Long, ItemTemplate> loadItemTemplates(Map<String, Object> itemDrops) {
        if (itemDrops == null || itemDrops.isEmpty()) return Map.of();
        List<Long> ids = itemDrops.keySet().stream().map(Long::parseLong).toList();
        return itemTemplateRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(ItemTemplate::getId, t -> t));
    }
}
