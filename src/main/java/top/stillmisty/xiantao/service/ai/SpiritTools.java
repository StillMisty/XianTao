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
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.vo.CellStatusVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectAllVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.fudi.vo.GiveGiftVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.fudi.vo.UpgradeCellVO;
import top.stillmisty.xiantao.domain.item.enums.InventoryCategory;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.beast.BeastBreedingService;
import top.stillmisty.xiantao.service.fudi.FarmService;
import top.stillmisty.xiantao.service.fudi.FudiService;
import top.stillmisty.xiantao.service.inventory.InventoryService;

/** 地灵可用的工具函数（Function Calling） */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritTools {

  private final FudiService fudiService;
  private final FarmService farmService;
  private final BeastBreedingService beastBreedingService;
  private final InventoryService inventoryService;

  @Tool(description = "查询福地地块状态，返回空位编号。种植/建造前必调")
  public GetCellStatusResponse getCellStatus() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      CellStatusVO result = fudiService.getCellStatus(userId);
      return new GetCellStatusResponse(
          result.totalCells(),
          result.occupiedCount(),
          result.emptyCount(),
          result.emptyCellIds(),
          null);
    } catch (Exception e) {
      log.error("查询地块状态失败", e);
      return new GetCellStatusResponse(0, 0, 0, List.of(), e.getMessage());
    }
  }

  @Tool(description = "查询背包物品。返回编号列表，后续可用编号进行种植/孵化等操作")
  public QueryBagResponse queryBag(@ToolParam(description = "要查询的类别") InventoryCategory category) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      List<ItemEntry> items;
      String bagError = null;
      switch (category) {
        case SEED -> items = inventoryService.getSeedInventory(userId);
        case EQUIPMENT -> items = inventoryService.getEquipmentInventory(userId);
        case BEAST_EGG -> items = inventoryService.getEggInventory(userId);
        default -> {
          var type = category.toItemType();
          if (type == null) {
            items = List.of();
            bagError = "不支持的类别：" + category.getChineseName();
          } else {
            items = inventoryService.getItemsByType(userId, type);
          }
        }
      }
      return new QueryBagResponse(category.getChineseName(), items, bagError);
    } catch (Exception e) {
      log.error("查询背包失败: category={}", category, e);
      return new QueryBagResponse(category.getChineseName(), List.of(), e.getMessage());
    }
  }

  @Tool(description = "在地块编号种植灵药。先调getCellStatus确认空位，再调queryBag选种子")
  @Transactional
  public PlantCropResponse plantCrop(
      @ToolParam(description = "地块编号") String position,
      @ToolParam(description = "种子编号或名称") String cropName) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      FarmCellVO result = farmService.plantCropByInput(userId, position, cropName);
      return new PlantCropResponse(position, cropName, result.getBaseGrowthHours(), null);
    } catch (Exception e) {
      log.error("种植灵药失败: position={}, cropName={}", position, cropName, e);
      return new PlantCropResponse(position, cropName, 0, e.getMessage());
    }
  }

  @Tool(description = "在地块编号建造灵田或兽栏")
  @Transactional
  public BuildCellResponse buildCell(
      @ToolParam(description = "地块编号") String position,
      @ToolParam(description = "地块类型") CellType cellType) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      fudiService.buildCell(userId, position, cellType);
      return new BuildCellResponse(position, cellType.getChineseName(), null);
    } catch (Exception e) {
      log.error("建造地块失败: position={}, cellType={}", position, cellType, e);
      return new BuildCellResponse(position, cellType.getChineseName(), e.getMessage());
    }
  }

  @Tool(description = "在兽栏地块孵化兽卵。先建兽栏，再调queryBag('兽卵')选卵")
  @Transactional
  public HatchBeastResponse hatchBeast(
      @ToolParam(description = "地块编号") String position,
      @ToolParam(description = "兽卵编号或名称") String eggName) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      PenCellVO result = beastBreedingService.hatchBeastByInput(userId, position, eggName);
      long hours =
          java.time.Duration.between(java.time.LocalDateTime.now(), result.getMatureTime())
              .toHours();
      return new HatchBeastResponse(
          position, result.getBeastName(), result.getQuality(), result.getTier(), hours, null);
    } catch (Exception e) {
      log.error("孵化兽卵失败: position={}, eggName={}", position, eggName, e);
      return new HatchBeastResponse(position, eggName, null, 0, 0, e.getMessage());
    }
  }

  @Tool(description = "拆除地块建筑，腾出空位。种错了作物或需要换类型时用。")
  @Transactional
  public RemoveCellResponse removeCell(@ToolParam(description = "地块编号") String position) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      var result = fudiService.removeCell(userId, position);
      return new RemoveCellResponse(position, result.type(), null);
    } catch (Exception e) {
      log.error("拆除地块失败: position={}", position, e);
      return new RemoveCellResponse(position, "", e.getMessage());
    }
  }

  @Tool(description = "收取地块产出（作物/兽栏）。传'all'全部收取")
  @Transactional
  public CollectProduceResponse collectProduce(
      @ToolParam(description = "地块编号或'all'") String position) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      if ("all".equalsIgnoreCase(position)) {
        CollectAllVO result = fudiService.collectAll(userId);
        return new CollectProduceResponse(
            "all", result.harvested(), result.collected(), result.totalItems(), null);
      } else {
        CollectVO result = fudiService.collect(userId, position);
        boolean isFarm = "FARM".equals(result.type());
        int harvested = isFarm ? 1 : 0;
        int collected = isFarm ? 0 : 1;
        int items = isFarm ? result.yield() : result.totalItems();
        return new CollectProduceResponse(position, harvested, collected, items, null);
      }
    } catch (Exception e) {
      log.error("收取产出失败: position={}", position, e);
      return new CollectProduceResponse(position, 0, 0, 0, e.getMessage());
    }
  }

  @Tool(description = "升级地块等级，需消耗灵石和材料。等级越高消耗越大")
  @Transactional
  public UpgradeCellResponse upgradeCell(@ToolParam(description = "地块编号") String position) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      UpgradeCellVO result = fudiService.upgradeCell(userId, position);
      return new UpgradeCellResponse(position, result.oldLevel(), result.newLevel(), null);
    } catch (Exception e) {
      log.error("升级地块失败: position={}", position, e);
      return new UpgradeCellResponse(position, 0, 0, e.getMessage());
    }
  }

  @Tool(description = "赠送物品给地灵")
  @Transactional
  public GiveGiftResponse giveGift(@ToolParam(description = "物品名称") String itemName) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      GiveGiftVO result = fudiService.giveGift(userId, itemName);
      return new GiveGiftResponse(result.itemName(), result.change(), result.reaction(), null);
    } catch (Exception e) {
      log.error("赠送礼物失败: itemName={}", itemName, e);
      return new GiveGiftResponse(itemName, 0, "", e.getMessage());
    }
  }

  @Tool(description = "玩家冒犯时调用。地灵自主判断")
  @Transactional
  public ReportOffenseResponse reportPlayerOffense(
      @ToolParam(description = "玩家说了什么") String reason,
      @ToolParam(description = "冒犯程度 1~5（1=轻微冒犯，5=严重冒犯）") int severity) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      int clampedSeverity = Math.clamp(severity, 1, 5);
      fudiService.adjustSpiritAffection(userId, -clampedSeverity);
      return new ReportOffenseResponse(reason, clampedSeverity, null);
    } catch (Exception e) {
      log.error("冒犯上报失败: reason={}", reason, e);
      return new ReportOffenseResponse(reason, 0, e.getMessage());
    }
  }

  @Tool(description = "更新地灵的情绪状态")
  @Transactional
  public UpdateEmotionResponse updateEmotion(
      @ToolParam(description = "新的情绪状态") EmotionState emotionState) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      fudiService.updateSpiritEmotion(userId, emotionState);
      log.debug("地灵情绪更新 - userId: {}, emotion: {}", userId, emotionState);
      return new UpdateEmotionResponse(emotionState.name(), null);
    } catch (Exception e) {
      log.error("更新情绪失败 - error: {}", e.getMessage());
      return new UpdateEmotionResponse("", e.getMessage());
    }
  }

  public record GetCellStatusResponse(
      @JsonPropertyDescription("总地块数") int totalCells,
      @JsonPropertyDescription("已占用数") int occupiedCount,
      @JsonPropertyDescription("空闲数") int emptyCount,
      @JsonPropertyDescription("空闲地块编号列表") List<Integer> emptyCellIds,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record QueryBagResponse(
      @JsonPropertyDescription("查询类别") String category,
      @JsonPropertyDescription("物品列表") List<ItemEntry> items,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record PlantCropResponse(
      @JsonPropertyDescription("地块编号") String position,
      @JsonPropertyDescription("作物名称") String cropName,
      @JsonPropertyDescription("基础生长时间（小时）") double baseGrowthHours,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record BuildCellResponse(
      @JsonPropertyDescription("地块编号") String position,
      @JsonPropertyDescription("地块类型") String cellType,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record HatchBeastResponse(
      @JsonPropertyDescription("地块编号") String position,
      @JsonPropertyDescription("兽名") String beastName,
      @JsonPropertyDescription("品质") String quality,
      @JsonPropertyDescription("阶位") int tier,
      @JsonPropertyDescription("成熟所需小时") long matureHours,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record RemoveCellResponse(
      @JsonPropertyDescription("地块编号") String position,
      @JsonPropertyDescription("地块类型") String type,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record CollectProduceResponse(
      @JsonPropertyDescription("地块编号或'all'") String position,
      @JsonPropertyDescription("收获的灵田数") int harvested,
      @JsonPropertyDescription("收取的兽栏数") int collected,
      @JsonPropertyDescription("总产出数") int totalItems,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record GiveGiftResponse(
      @JsonPropertyDescription("物品名称") String itemName,
      @JsonPropertyDescription("好感度变化") int affectionChange,
      @JsonPropertyDescription("地灵反应") String reaction,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record UpgradeCellResponse(
      @JsonPropertyDescription("地块编号") String position,
      @JsonPropertyDescription("升级前等级") int oldLevel,
      @JsonPropertyDescription("升级后等级") int newLevel,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record ReportOffenseResponse(
      @JsonPropertyDescription("冒犯原因") String reason,
      @JsonPropertyDescription("冒犯程度（1~5）") int severity,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}

  public record UpdateEmotionResponse(
      @JsonPropertyDescription("更新后的情绪状态") String emotionState,
      @JsonPropertyDescription("错误信息，null 表示成功") String error) {}
}
