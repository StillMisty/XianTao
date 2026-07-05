package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.vo.CellStatusVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectAllVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.fudi.vo.UpgradeCellVO;
import top.stillmisty.xiantao.domain.item.enums.InventoryCategory;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.fudi.FarmService;
import top.stillmisty.xiantao.service.fudi.FudiService;
import top.stillmisty.xiantao.service.inventory.InventoryService;

/** 福地地块操作工具 — 查询、建造、拆除、升级、种植、收取 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritCellTools {

  private final ToolExecutor toolExecutor;
  private final FudiService fudiService;
  private final FarmService farmService;
  private final InventoryService inventoryService;

  @Tool(description = "查询福地土地使用情况：有多少块地、哪些空着可用。返回空地块编号列表供种植/建造选择")
  public CheckFudiCellsResponse checkFudiCells() {
    return toolExecutor.execute(
        "checkFudiCells",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          CellStatusVO r = fudiService.getCellStatus(userId);
          return new CheckFudiCellsResponse(
              r.totalCells(), r.occupiedCount(), r.emptyCount(), r.emptyCellIds());
        });
  }

  @Tool(description = "按类别查看背包物品。返回物品列表含编号、名称和数量。" + InventoryCategory.PARAM_DESCRIPTION)
  public CheckPlayerBagResponse checkPlayerBag(
      @ToolParam(description = InventoryCategory.PARAM_DESCRIPTION) InventoryCategory category) {
    return toolExecutor.execute(
        "checkPlayerBag",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          List<ItemEntry> items =
              category == InventoryCategory.ALL
                  ? getAllItems(userId)
                  : getItemsByCategory(userId, category);
          return new CheckPlayerBagResponse(category.getChineseName(), items);
        });
  }

  @Tool(description = "在指定灵田地块播种。cropName 是作物名称或种子编号（需先从 checkPlayerBag(SEED) 获取）")
  @Transactional
  public PlantCropResponse plantCrop(
      @ToolParam(description = "地块编号") String position,
      @ToolParam(description = "作物名称或种子编号") String cropName) {
    return toolExecutor.execute(
        "plantCrop",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          FarmCellVO r = farmService.plantCropByInputInternal(userId, position, cropName);
          return new PlantCropResponse(position, cropName, r.baseGrowthHours());
        });
  }

  @Tool(description = "在空地块上建造灵田或兽栏")
  @Transactional
  public BuildCellResponse buildCell(
      @ToolParam(description = "空地块编号") String position,
      @ToolParam(description = CellType.PARAM_DESCRIPTION) CellType cellType) {
    return toolExecutor.execute(
        "buildCell",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          fudiService.buildCellInternal(userId, position, cellType);
          return new BuildCellResponse(position, cellType.getChineseName());
        });
  }

  @Tool(description = "拆除指定地块上的灵田或兽栏，返还为空地。不可拆除已种植或已孵化的地块")
  @Transactional
  public RemoveCellResponse removeCell(@ToolParam(description = "要拆除的地块编号") String position) {
    return toolExecutor.execute(
        "removeCell",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          var r = fudiService.removeCellInternal(userId, position);
          return new RemoveCellResponse(position, r.type());
        });
  }

  @Tool(description = "消耗灵石给地块升级。等级越高产量越大、成熟越快")
  @Transactional
  public UpgradeCellResponse upgradeCell(@ToolParam(description = "要升级的地块编号") String position) {
    return toolExecutor.execute(
        "upgradeCell",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          UpgradeCellVO r = fudiService.upgradeCellInternal(userId, position);
          return new UpgradeCellResponse(position, r.oldLevel(), r.newLevel());
        });
  }

  @Tool(description = "收取成熟作物和灵兽产出。传 'all' 批量收取所有成熟地块")
  @Transactional
  public CollectProduceResponse collectProduce(
      @ToolParam(description = "地块编号，或 'all' 全部收取") String position) {
    return toolExecutor.execute(
        "collectProduce",
        () -> {
          Long userId = UserContext.requireCurrentUserId();
          if ("all".equalsIgnoreCase(position)) {
            CollectAllVO r = fudiService.collectAllInternal(userId);
            return new CollectProduceResponse("all", r.harvested(), r.collected(), r.totalItems());
          }
          CollectVO r = fudiService.collectInternal(userId, position);
          boolean isFarm = "FARM".equals(r.type());
          int harvested = isFarm ? 1 : 0;
          int collected = isFarm ? 0 : 1;
          int items = isFarm ? r.yield() : r.totalItems();
          return new CollectProduceResponse(position, harvested, collected, items);
        });
  }

  // ===== 私有辅助 =====

  private List<ItemEntry> getItemsByCategory(Long userId, InventoryCategory category) {
    return switch (category) {
      case SEED -> inventoryService.getSeedInventory(userId);
      case EQUIPMENT -> inventoryService.getEquipmentInventory(userId);
      case BEAST_EGG -> inventoryService.getEggInventory(userId);
      default -> {
        var type = category.toItemType();
        yield type != null
            ? inventoryService.getItemsByType(userId, type)
            : java.util.List.<ItemEntry>of();
      }
    };
  }

  private List<ItemEntry> getAllItems(Long userId) {
    List<ItemEntry> all = new java.util.ArrayList<>();
    for (var cat : InventoryCategory.values()) {
      if (cat == InventoryCategory.ALL) continue;
      all.addAll(getItemsByCategory(userId, cat));
    }
    return all;
  }

  // ===== Response records =====

  public record CheckFudiCellsResponse(
      @JsonPropertyDescription("福地总地块数") int totalCells,
      @JsonPropertyDescription("已被占用（灵田/兽栏）的地块数") int occupiedCount,
      @JsonPropertyDescription("尚未开发可用的空地块数") int emptyCount,
      @JsonPropertyDescription("可用的空地块编号列表") java.util.List<Integer> emptyCellIds) {}

  public record CheckPlayerBagResponse(
      @JsonPropertyDescription("查询的类别中文名") String category,
      @JsonPropertyDescription("物品列表")
          java.util.List<top.stillmisty.xiantao.domain.item.vo.ItemEntry> items) {}

  public record BuildCellResponse(
      @JsonPropertyDescription("建造的地块编号") String position,
      @JsonPropertyDescription("建造后的地块类型：灵田 或 兽栏") String cellType) {}

  public record RemoveCellResponse(
      @JsonPropertyDescription("被拆除的地块编号") String position,
      @JsonPropertyDescription("拆除前的地块类型（灵田/兽栏）") String type) {}

  public record UpgradeCellResponse(
      @JsonPropertyDescription("被升级的地块编号") String position,
      @JsonPropertyDescription("升级前等级") int oldLevel,
      @JsonPropertyDescription("升级后等级") int newLevel) {}

  public record PlantCropResponse(
      @JsonPropertyDescription("播种的地块编号") String position,
      @JsonPropertyDescription("种下的作物名称") String cropName,
      @JsonPropertyDescription("基础生长时间（小时）") double baseGrowthHours) {}

  public record CollectProduceResponse(
      @JsonPropertyDescription("收取的地块编号，或 'all' 表示全部") String position,
      @JsonPropertyDescription("收获的灵田数量") int harvested,
      @JsonPropertyDescription("收取的兽栏数量") int collected,
      @JsonPropertyDescription("总产出物品件数") int totalItems) {}
}
