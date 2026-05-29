package top.stillmisty.xiantao.handle.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.command.CommandEntry;
import top.stillmisty.xiantao.domain.command.CommandGroup;
import top.stillmisty.xiantao.domain.item.enums.InventoryCategory;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.vo.AttributeChange;
import top.stillmisty.xiantao.domain.item.vo.InventorySummaryVO;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.CommandHandlerHelper;
import top.stillmisty.xiantao.handle.TextFormat;
import top.stillmisty.xiantao.service.inventory.DiscardService;
import top.stillmisty.xiantao.service.inventory.EquipmentService;
import top.stillmisty.xiantao.service.inventory.InventoryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCommandHandler implements CommandGroup {

  private final InventoryService inventoryService;
  private final EquipmentService equipmentService;
  private final DiscardService discardService;

  // ===================== 统一处理方法 =====================

  public String handleInventory(PlatformType platform, String openId, TextFormat fmt) {
    log.debug("处理背包查询 - Platform: {}, OpenId: {}", platform, openId);
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getInventorySummary(platform, openId),
        fmt,
        vo -> formatInventorySummary(vo, fmt));
  }

  public String handleSeedInventory(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getSeedInventory(platform, openId),
        fmt,
        entries -> formatItemList("种子", entries, fmt));
  }

  public String handleEquipmentInventory(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getEquipmentInventory(platform, openId),
        fmt,
        entries -> formatItemList("装备", entries, fmt));
  }

  public String handleEggInventory(PlatformType platform, String openId, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getEggInventory(platform, openId),
        fmt,
        entries -> formatItemList("兽卵", entries, fmt));
  }

  public String handleInventoryByCategory(
      PlatformType platform, String openId, String category, TextFormat fmt) {
    for (var cat : InventoryCategory.values()) {
      if (cat.getChineseName().equals(category)) {
        if (cat == InventoryCategory.EQUIPMENT) {
          return handleEquipmentInventory(platform, openId, fmt);
        }
        var type = cat.toItemType();
        if (type != null) {
          return handleItemTypeInventory(platform, openId, cat.getChineseName(), type, fmt);
        }
      }
    }
    var validCategories =
        Arrays.stream(InventoryCategory.values())
            .map(InventoryCategory::getChineseName)
            .collect(Collectors.joining("、"));
    return "未知分类，可选：" + validCategories;
  }

  private String handleItemTypeInventory(
      PlatformType platform, String openId, String categoryName, ItemType type, TextFormat fmt) {
    return CommandHandlerHelper.safeCall(
        () -> inventoryService.getItemsByType(platform, openId, type),
        fmt,
        entries -> formatItemList(categoryName, entries, fmt));
  }

  public String handleEquip(PlatformType platform, String openId, String itemName, TextFormat fmt) {
    log.debug("处理装备穿戴 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);
    return CommandHandlerHelper.safeCall(
        () -> equipmentService.equipItem(platform, openId, itemName),
        fmt,
        vo -> vo.success() ? formatEquipResult(vo, fmt) : vo.message());
  }

  public String handleUnequip(
      PlatformType platform, String openId, String slotName, TextFormat fmt) {
    log.debug("处理装备卸下 - Platform: {}, OpenId: {}, SlotName: {}", platform, openId, slotName);
    return CommandHandlerHelper.safeCall(
        () -> equipmentService.unequipItem(platform, openId, slotName),
        fmt,
        vo -> vo.isSuccess() ? formatUnequipResult(vo, fmt) : vo.getMessage());
  }

  public String handleDiscard(
      PlatformType platform, String openId, String itemName, TextFormat fmt) {
    log.debug("处理丢弃 - Platform: {}, OpenId: {}, ItemName: {}", platform, openId, itemName);
    return CommandHandlerHelper.safeCall(
        () -> discardService.discardItem(platform, openId, itemName), fmt, msg -> msg);
  }

  // ===================== 格式化方法 =====================

  private String formatItemList(String title, List<ItemEntry> entries, TextFormat fmt) {
    if (entries.isEmpty()) {
      return fmt.heading(title + "列表（空）").trim();
    }
    var sb = new StringBuilder(fmt.heading(title + "列表"));
    for (var e : entries) {
      sb.append(e.index()).append(". ").append(e.name());
      if (e.quantity() > 1) sb.append(" x").append(e.quantity());
      if (!e.metadata().isBlank()) sb.append(" [").append(e.metadata()).append("]");
      sb.append("\n");
    }
    return sb.toString().strip();
  }

  private String formatInventorySummary(InventorySummaryVO inventory, TextFormat fmt) {
    var sb = new StringBuilder(fmt.heading("背包"));
    if (inventory.equipment() != null && !inventory.equipment().isEmpty()) {
      sb.append("\n").append(fmt.heading("装备"));
      for (var e : inventory.equipment()) {
        sb.append("\n").append(fmt.listItem(e.name() + " [" + e.metadata() + "]"));
      }
    }
    if (inventory.itemsByType() != null && !inventory.itemsByType().isEmpty()) {
      for (var entry : inventory.itemsByType().entrySet()) {
        sb.append("\n").append(fmt.heading(entry.getKey().getName()));
        for (var e : entry.getValue()) {
          sb.append("\n").append(fmt.listItem(e.name() + " x" + e.quantity()));
        }
      }
    }
    sb.append("\n").append(fmt.listItem("灵石：" + inventory.spiritStones()));
    return sb.toString();
  }

  private String formatEquipResult(
      top.stillmisty.xiantao.domain.item.vo.EquipResult result, TextFormat fmt) {
    return formatAttributeChangeResult(result.message(), result.attributeChange(), fmt);
  }

  private String formatUnequipResult(
      top.stillmisty.xiantao.domain.item.vo.UnequipResult result, TextFormat fmt) {
    return formatAttributeChangeResult(result.getMessage(), result.getAttributeChange(), fmt);
  }

  private String formatAttributeChangeResult(
      String message, AttributeChange change, TextFormat fmt) {
    StringBuilder sb = new StringBuilder();
    sb.append(message).append("\n");
    if (change != null) {
      sb.append("\n");
      sb.append(fmt.heading("属性变化"));
      if (change.strChange() != 0)
        sb.append(formatAttrChange("力道", change.strChange())).append("\n");
      if (change.conChange() != 0)
        sb.append(formatAttrChange("根骨", change.conChange())).append("\n");
      if (change.agiChange() != 0)
        sb.append(formatAttrChange("身法", change.agiChange())).append("\n");
      if (change.wisChange() != 0)
        sb.append(formatAttrChange("悟性", change.wisChange())).append("\n");
      if (change.attackChange() != 0)
        sb.append(formatAttrChange("攻击", change.attackChange())).append("\n");
      if (change.defenseChange() != 0)
        sb.append(formatAttrChange("防御", change.defenseChange())).append("\n");
      if (change.maxHpChange() != 0) sb.append(formatAttrChange("HP上限", change.maxHpChange()));
    }
    return sb.toString();
  }

  private String formatAttrChange(String attrName, int change) {
    String sign = change > 0 ? "+" : "";
    return String.format("  %s：%s%d", attrName, sign, change);
  }

  @Override
  public String groupName() {
    return "背包";
  }

  @Override
  public String groupSummary() {
    return "物品管理、装备穿戴";
  }

  @Override
  public String groupDescription() {
    return "背包查看、装备穿戴/卸下、物品丢弃";
  }

  @Override
  public List<CommandEntry> commands() {
    return List.of(
        new CommandEntry("背包", "查看背包汇总", "背包"),
        new CommandEntry("背包 「分类」", "查看分类物品（种子/装备/兽卵/锻材/丹药/药材/法决玉简/丹方卷轴/锻造图纸/灵兽精华）", "背包 丹药"),
        new CommandEntry("装备 「物品」", "穿戴装备", "装备 玄铁剑"),
        new CommandEntry("卸下 「部位/物品」", "卸下装备（支持部位名或物品名/编号）", "卸下 武器"),
        new CommandEntry("丢弃 「物品」", "丢弃物品或装备", "丢弃 玄铁剑"));
  }
}
