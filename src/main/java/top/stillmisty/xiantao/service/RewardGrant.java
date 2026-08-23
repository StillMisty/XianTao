package top.stillmisty.xiantao.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.item.entity.ItemTemplate;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.monster.vo.DropItem;
import top.stillmisty.xiantao.infrastructure.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.service.inventory.EquipmentService;
import top.stillmisty.xiantao.service.inventory.StackableItemService;

/** 奖励发放 — 将掉落/奖励统一入账：装备走实例化锻造，其余按模板类型入堆叠库存。 */
@Component
@RequiredArgsConstructor
public class RewardGrant {

  private final ItemTemplateRepository itemTemplateRepository;
  private final EquipmentService equipmentService;
  private final StackableItemService stackableItemService;

  /** 发放一批奖励。模板缺失时以「未知物品」名义入包，数量与类型保持原语义。 */
  @Transactional
  public void grant(Long userId, List<DropItem> drops) {
    if (drops == null || drops.isEmpty()) return;

    List<Long> itemTemplateIds =
        drops.stream()
            .filter(d -> d.type() != DropItem.DropType.EQUIPMENT)
            .map(DropItem::templateId)
            .distinct()
            .toList();
    Map<Long, ItemTemplate> templateMap =
        itemTemplateIds.isEmpty()
            ? Map.of()
            : itemTemplateRepository.findByIds(itemTemplateIds).stream()
                .collect(java.util.stream.Collectors.toMap(ItemTemplate::getId, t -> t));

    for (DropItem drop : drops) {
      if (drop.type() == DropItem.DropType.EQUIPMENT) {
        equipmentService.createEquipment(userId, drop.templateId());
      } else {
        ItemTemplate tmpl = templateMap.get(drop.templateId());
        ItemType type = tmpl != null ? tmpl.getType() : ItemType.MATERIAL;
        String name = resolveName(drop, tmpl);
        stackableItemService.addStackableItem(
            userId, drop.templateId(), type, name, drop.quantity());
      }
    }
  }

  /** 名称优先取奖励自带（悬赏奖励在生成时快照），缺失时回落模板名，再兜底「未知物品」。 */
  private static String resolveName(DropItem drop, @Nullable ItemTemplate tmpl) {
    if (drop.name() != null && !drop.name().isBlank()) return drop.name();
    if (tmpl != null) return tmpl.getName();
    return "未知物品";
  }
}
