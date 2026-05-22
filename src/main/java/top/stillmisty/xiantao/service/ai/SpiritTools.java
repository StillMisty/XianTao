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
import top.stillmisty.xiantao.domain.fudi.vo.GiveGiftVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.fudi.vo.UpgradeCellVO;
import top.stillmisty.xiantao.domain.item.enums.InventoryCategory;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.service.*;
import top.stillmisty.xiantao.service.beast.BeastBreedingService;
import top.stillmisty.xiantao.service.fudi.FarmService;
import top.stillmisty.xiantao.service.fudi.FudiService;
import top.stillmisty.xiantao.service.inventory.InventoryService;

/** 地灵可用的工具函数（Function Calling） 这些工具会被 LLM 调用，以执行福地相关的操作 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritTools {

  private final FudiService fudiService;
  private final FarmService farmService;
  private final BeastBreedingService beastBreedingService;
  private final InventoryService inventoryService;

  /** 查询福地地块状态工具 */
  @Tool(description = "查询福地地块状态，返回空位编号。种植/建造前必调")
  public GetCellStatusResponse getCellStatus() {
    try {
      Long userId = UserContext.requireCurrentUserId();
      CellStatusVO result = fudiService.getCellStatus(userId);

      String message;
      if (result.emptyCount() == 0) {
        message = String.format("福地已满（共%d个地块，全部占用）。", result.totalCells());
      } else {
        message =
            String.format(
                "福地共 %d 个地块，已占用 %d 个，还有 %d 个空位。可用地块编号：%s",
                result.totalCells(),
                result.occupiedCount(),
                result.emptyCount(),
                result.emptyCellIds().toString());
      }

      return new GetCellStatusResponse(
          true,
          message,
          result.totalCells(),
          result.occupiedCount(),
          result.emptyCount(),
          result.emptyCellIds());
    } catch (Exception e) {
      log.error("查询地块状态失败", e);
      return new GetCellStatusResponse(
          false, "查询失败：" + e.getMessage(), 0, 0, 0, java.util.List.of());
    }
  }

  /** 查询玩家背包工具 */
  @Tool(description = "查询背包物品。返回编号列表，后续可用编号进行种植/孵化等操作")
  public QueryBagResponse queryBag(@ToolParam(description = "要查询的类别") InventoryCategory category) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      String message =
          switch (category) {
            case SEED -> {
              var list = inventoryService.getSeedInventory(userId);
              if (list.isEmpty()) yield "背包中没有种子。";
              yield formatInventoryList("种子", list);
            }
            case EQUIPMENT -> {
              var list = inventoryService.getEquipmentInventory(userId);
              if (list.isEmpty()) yield "背包中没有可装备的物品。";
              var sb = new StringBuilder("【装备列表】\n");
              for (var e : list) {
                sb.append(e.index()).append(". ").append(e.name());
                sb.append(" [").append(e.metadata()).append("]\n");
              }
              yield sb.toString().strip();
            }
            case BEAST_EGG -> {
              var list = inventoryService.getEggInventory(userId);
              if (list.isEmpty()) yield "背包中没有兽卵。";
              yield formatInventoryList("兽卵", list);
            }
            default -> {
              var type = category.toItemType();
              if (type == null) yield "不支持的类别：" + category.getChineseName();
              var list = inventoryService.getItemsByType(userId, type);
              if (list.isEmpty()) yield "背包中没有" + category.getChineseName() + "。";
              yield formatInventoryList(category.getChineseName(), list);
            }
          };
      return new QueryBagResponse(true, message, category.getChineseName());
    } catch (Exception e) {
      log.error("查询背包失败: category={}", category, e);
      return new QueryBagResponse(false, "查询背包失败：" + e.getMessage(), category.getChineseName());
    }
  }

  private static String formatInventoryList(String category, List<ItemEntry> list) {
    var sb = new StringBuilder("【" + category + "列表】\n");
    for (var e : list) {
      sb.append(e.index()).append(". ").append(e.name());
      if (e.quantity() > 1) sb.append(" x").append(e.quantity());
      sb.append(" [").append(e.metadata()).append("]\n");
    }
    return sb.toString().strip();
  }

  /** 种植灵药工具 */
  @Tool(description = "在地块编号种植灵药。先调getCellStatus确认空位，再调queryBag选种子")
  @Transactional
  public PlantCropResponse plantCrop(
      @ToolParam(description = "地块编号") String position,
      @ToolParam(description = "种子编号或名称") String cropName) {
    try {
      Long userId = UserContext.requireCurrentUserId();

      FarmCellVO result = farmService.plantCropByInput(userId, position, cropName);

      return new PlantCropResponse(
          true,
          String.format(
              "已在地块 %s 种植%s，预计 %.1f 小时后成熟。", position, cropName, result.getBaseGrowthHours()),
          position,
          cropName);
    } catch (Exception e) {
      log.error("种植灵药失败: position={}, cropName={}", position, cropName, e);
      return new PlantCropResponse(false, "种植失败：" + e.getMessage(), position, cropName);
    }
  }

  /** 建造地块工具 */
  @Tool(description = "在地块编号建造灵田或兽栏")
  @Transactional
  public BuildCellResponse buildCell(
      @ToolParam(description = "地块编号") String position,
      @ToolParam(description = "地块类型") CellType cellType) {
    try {
      Long userId = UserContext.requireCurrentUserId();

      fudiService.buildCell(userId, position, cellType);

      return new BuildCellResponse(
          true,
          String.format("已在地块 %s 建造%s。", position, cellType.getChineseName()),
          position,
          cellType.getChineseName());
    } catch (Exception e) {
      log.error("建造地块失败: position={}, cellType={}", position, cellType, e);
      return new BuildCellResponse(
          false, "建造失败：" + e.getMessage(), position, cellType.getChineseName());
    }
  }

  /** 孵化兽卵工具 */
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
          true,
          String.format(
              "已在地块 %s 孵化%s（%s/T%d），预计 %d 小时后成熟。",
              position, result.getBeastName(), result.getQuality(), result.getTier(), hours),
          position,
          result.getBeastName());
    } catch (Exception e) {
      log.error("孵化兽卵失败: position={}, eggName={}", position, eggName, e);
      return new HatchBeastResponse(false, "孵化失败：" + e.getMessage(), position, eggName);
    }
  }

  /** 拆除地块工具 */
  @Tool(description = "拆除地块建筑，腾出空位。种错了作物或需要换类型时用。")
  @Transactional
  public RemoveCellResponse removeCell(@ToolParam(description = "地块编号") String position) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      var result = fudiService.removeCell(userId, position);

      return new RemoveCellResponse(
          true, String.format("已拆除地块 %s 的%s。", position, result.type()), position);
    } catch (Exception e) {
      log.error("拆除地块失败: position={}", position, e);
      return new RemoveCellResponse(false, "拆除失败：" + e.getMessage(), position);
    }
  }

  /** 收取地块产出的统一工具（灵田收获 + 兽栏收取） */
  @Tool(description = "收取地块产出（作物/兽栏）。传'all'全部收取")
  @Transactional
  public CollectProduceResponse collectProduce(
      @ToolParam(description = "地块编号或'all'") String position) {
    try {
      Long userId = UserContext.requireCurrentUserId();

      if ("all".equalsIgnoreCase(position)) {
        CollectAllVO result = fudiService.collectAll(userId);

        if (result.totalItems() == 0) {
          return new CollectProduceResponse(true, "现在没有可收取的内容。", position, 0);
        }
        return new CollectProduceResponse(
            true,
            String.format(
                "已收获 %d 块灵田、收取 %d 个兽栏，共获得 %d 份物资。",
                result.harvested(), result.collected(), result.totalItems()),
            position,
            result.totalItems());
      } else {
        CollectVO result = fudiService.collect(userId, position);
        int items;
        String message;
        if ("FARM".equals(result.type())) {
          items = result.yield();
          message = String.format("已收获地块 %s 的%s，获得 %d 份。", position, result.cropName(), items);
        } else {
          items = result.totalItems();
          message = String.format("已从地块 %s 收取了 %d 件%s产出。", position, items, result.beastName());
        }

        return new CollectProduceResponse(true, message, position, items);
      }
    } catch (Exception e) {
      log.error("收取产出失败: position={}", position, e);
      return new CollectProduceResponse(false, "收取失败：" + e.getMessage(), position, 0);
    }
  }

  /** 升级地块工具 */
  @Tool(description = "升级地块等级，需消耗灵石和材料。等级越高消耗越大")
  @Transactional
  public UpgradeCellResponse upgradeCell(@ToolParam(description = "地块编号") String position) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      UpgradeCellVO result = fudiService.upgradeCell(userId, position);
      return new UpgradeCellResponse(
          true,
          String.format(
              "已将地块 %s 的%s从 Lv%d 升级至 Lv%d。",
              position, result.type(), result.oldLevel(), result.newLevel()),
          position,
          result.oldLevel(),
          result.newLevel());
    } catch (Exception e) {
      log.error("升级地块失败: position={}", position, e);
      return new UpgradeCellResponse(false, "升级失败：" + e.getMessage(), position, 0, 0);
    }
  }

  /** 赠送礼物工具 */
  @Tool(description = "赠送物品给地灵")
  @Transactional
  public GiveGiftResponse giveGift(@ToolParam(description = "物品名称") String itemName) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      GiveGiftVO result = fudiService.giveGift(userId, itemName);

      String message;
      if (result.isLiked()) {
        message = String.format("地灵收到%s，非常开心！好感度 %+d", result.itemName(), result.change());
      } else if (result.isDisliked()) {
        message = String.format("地灵嫌弃地看了一眼%s，好感度 %d", result.itemName(), result.change());
      } else {
        message = String.format("地灵礼貌地收下了%s，好感度 %+d", result.itemName(), result.change());
      }

      return new GiveGiftResponse(
          true, message, result.itemName(), result.change(), result.reaction());
    } catch (Exception e) {
      log.error("赠送礼物失败: itemName={}", itemName, e);
      return new GiveGiftResponse(false, "送礼失败：" + e.getMessage(), itemName, 0, "");
    }
  }

  /** 冒犯上报工具（由 LLM 主动调用） */
  @Tool(description = "玩家冒犯时调用。地灵自主判断")
  @Transactional
  public ReportOffenseResponse reportPlayerOffense(
      @ToolParam(description = "玩家说了什么") String reason,
      @ToolParam(description = "冒犯程度 1~5（1=轻微冒犯，5=严重冒犯）") int severity) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      int clampedSeverity = Math.clamp(severity, 1, 5);
      fudiService.adjustSpiritAffection(userId, -clampedSeverity);
      return new ReportOffenseResponse(true, reason, clampedSeverity);
    } catch (Exception e) {
      log.error("冒犯上报失败: reason={}", reason, e);
      return new ReportOffenseResponse(false, reason, 0);
    }
  }

  // ===================== 响应 Record 定义 =====================

  public record QueryBagResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("物品列表文本") String message,
      @JsonPropertyDescription("查询的类别") String category) {}

  public record GetCellStatusResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("地块总数") int totalCells,
      @JsonPropertyDescription("已占地块数") int occupiedCount,
      @JsonPropertyDescription("空位数") int emptyCount,
      @JsonPropertyDescription("可用地块编号列表") java.util.List<Integer> emptyCellIds) {}

  public record PlantCropResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("种植地块编号") String position,
      @JsonPropertyDescription("作物名称") String cropName) {}

  public record BuildCellResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("建造地块编号") String position,
      @JsonPropertyDescription("地块类型") String cellType) {}

  public record HatchBeastResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("孵化地块编号") String position,
      @JsonPropertyDescription("灵兽名称") String beastName) {}

  public record RemoveCellResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("拆除地块编号") String position) {}

  public record CollectProduceResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("收取地块编号") String position,
      @JsonPropertyDescription("收取数量") int collected) {}

  public record GiveGiftResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("物品名称") String itemName,
      @JsonPropertyDescription("好感度变化") int affectionChange,
      @JsonPropertyDescription("地灵反应") String reaction) {}

  public record UpgradeCellResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("地块编号") String position,
      @JsonPropertyDescription("旧等级") int oldLevel,
      @JsonPropertyDescription("新等级") int newLevel) {}

  public record ReportOffenseResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("冒犯原因") String reason,
      @JsonPropertyDescription("冒犯程度 1~5") int severity) {}
}
