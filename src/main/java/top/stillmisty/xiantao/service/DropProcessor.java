package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.monster.entity.MonsterTemplate;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.domain.monster.vo.DropItem.DropType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class DropProcessor {

    private final ItemTemplateRepository itemTemplateRepository;
    private final EquipmentService equipmentService;
    private final StackableItemService stackableItemService;

    public List<DropItem> processMonsterDrops(MonsterTemplate tmpl) {
        List<DropItem> drops = new ArrayList<>();
        Map<String, Object> dropTable = tmpl.getDropTable();
        if (dropTable == null || dropTable.isEmpty()) return drops;

        // 预加载所有可能掉落的物品模板，避免 N+1 查询
        List<Long> templateIds = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> equipmentDrops = (Map<String, Object>) dropTable.get("equipment");
        if (equipmentDrops != null) {
            equipmentDrops.keySet().forEach(k -> templateIds.add(Long.parseLong(k)));
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> itemDrops = (Map<String, Object>) dropTable.get("items");
        if (itemDrops != null) {
            itemDrops.keySet().forEach(k -> templateIds.add(Long.parseLong(k)));
        }
        Map<Long, ItemTemplate> templateMap = templateIds.isEmpty() ? Map.of()
                : itemTemplateRepository.findByIds(templateIds).stream()
                        .collect(java.util.stream.Collectors.toMap(ItemTemplate::getId, t -> t));

        if (equipmentDrops != null) {
            for (var entry : equipmentDrops.entrySet()) {
                Long templateId = Long.parseLong(entry.getKey());
                int weight = ((Number) entry.getValue()).intValue();
                if (ThreadLocalRandom.current().nextInt(100) < weight) {
                    ItemTemplate tmplItem = templateMap.get(templateId);
                    if (tmplItem != null) {
                        drops.add(new DropItem(DropItem.DropType.EQUIPMENT, templateId, tmplItem.getName(), 1));
                    }
                }
            }
        }

        if (itemDrops != null) {
            for (var entry : itemDrops.entrySet()) {
                Long templateId = Long.parseLong(entry.getKey());
                int weight = ((Number) entry.getValue()).intValue();
                if (ThreadLocalRandom.current().nextInt(100) < weight) {
                    ItemTemplate tmplItem = templateMap.get(templateId);
                    if (tmplItem != null) {
                        int qty = 1 + ThreadLocalRandom.current().nextInt(3);
                        drops.add(new DropItem(DropItem.DropType.ITEM, templateId, tmplItem.getName(), qty));
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
}
