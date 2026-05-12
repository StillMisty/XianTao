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
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.CellStatusVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectAllVO;
import top.stillmisty.xiantao.domain.fudi.vo.CollectVO;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.fudi.vo.GiveGiftVO;
import top.stillmisty.xiantao.domain.fudi.vo.PenCellVO;
import top.stillmisty.xiantao.domain.item.enums.InventoryCategory;
import top.stillmisty.xiantao.domain.item.vo.ItemEntry;
import top.stillmisty.xiantao.service.*;

/** 地灵可用的工具函数（Function Calling） 这些工具会被 LLM 调用，以执行福地相关的操作 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritTools {

  private final FudiService fudiService;
  private final FarmService farmService;
  private final BeastService beastService;
  private final InventoryService inventoryService;
  private final SpiritRepository spiritRepository;

  /** 查询福地地块状态工具 */
  @Tool(description = "查询福地的地块布局状态，包括哪些地块编号是空的、哪些已被占用。在种植或建造前应该先调用此工具了解可用地块编号。")
  public GetCellStatusResponse getCellStatus() {
    try {
      Long userId = getCurrentUserId();
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
  @Tool(description = "查询玩家背包中的物品列表。可以查看种子、装备或兽卵。返回编号列表，后续可以使用编号代替名称来操作物品。")
  public String queryBag(@ToolParam(description = "要查询的类别") InventoryCategory category) {
    try {
      Long userId = getCurrentUserId();
      return switch (category) {
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
      };
    } catch (Exception e) {
      log.error("查询背包失败: category={}", category, e);
      return "查询背包失败：" + e.getMessage();
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
  @Tool(
      description =
          "在福地的指定地块编号种植灵药。需要先调用getCellStatus确认地块为空，再调用queryBag查看可用种子。提供地块编号（如1、2等）和种子名称或编号。")
  public PlantCropResponse plantCrop(
      @ToolParam(description = "种植地块编号，如'1'、'2'等") String position,
      @ToolParam(description = "种子编号或名称。先调用queryBag('种子')查看编号，例如'1'、'2'，或直接使用名称如'灵草种子'")
          String cropName) {
    try {
      Long userId = getCurrentUserId();

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
  @Tool(description = "在福地的指定地块编号建造地块，可以建造灵田或兽栏。")
  public BuildCellResponse buildCell(
      @ToolParam(description = "建造地块编号，如'1'、'2'等") String position,
      @ToolParam(description = "地块类型") CellType cellType) {
    try {
      Long userId = getCurrentUserId();

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
  @Tool(description = "在福地的指定兽栏地块孵化兽卵。需要先建造兽栏，再调用queryBag('兽卵')查看可用兽卵。提供地块编号和兽卵名称或编号。")
  public HatchBeastResponse hatchBeast(
      @ToolParam(description = "孵化地块编号，如'1'、'2'等") String position,
      @ToolParam(description = "兽卵编号或名称。先调用queryBag('兽卵')查看编号，例如'1'、'2'，或直接使用名称如'火凤卵'")
          String eggName) {
    try {
      Long userId = getCurrentUserId();

      PenCellVO result = beastService.hatchBeastByInput(userId, position, eggName);

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
  @Tool(description = "拆除福地中指定地块编号的建筑。")
  public RemoveCellResponse removeCell(
      @ToolParam(description = "拆除地块编号，如'1'、'2'等") String position) {
    try {
      Long userId = getCurrentUserId();
      var result = fudiService.removeCell(userId, position);

      return new RemoveCellResponse(
          true, String.format("已拆除地块 %s 的%s。", position, result.type()), position);
    } catch (Exception e) {
      log.error("拆除地块失败: position={}", position, e);
      return new RemoveCellResponse(false, "拆除失败：" + e.getMessage(), position);
    }
  }

  /** 收取地块产出的统一工具（灵田收获 + 兽栏收取） */
  @Tool(description = "收取福地中指定地块的产出物（统一收取灵田作物和兽栏产出）。编号可以是具体数字或'all'表示全部收取。")
  public CollectProduceResponse collectProduce(
      @ToolParam(description = "收取地块编号，如'1'、'2'，或'all'表示全部收取") String position) {
    try {
      Long userId = getCurrentUserId();

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

  /** 赠送礼物工具 */
  @Tool(description = "赠送背包中的物品给地灵。当地灵表示喜欢某物或玩家主动送礼时调用。参数为物品名称。")
  public GiveGiftResponse giveGift(@ToolParam(description = "物品名称，如'精铁剑'、'灵石'等") String itemName) {
    try {
      Long userId = getCurrentUserId();
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
  @Tool(description = "当玩家说了不礼貌、冒犯、或让地灵不开心的话时调用此工具。由地灵自主判断是否需要调用。")
  @Transactional
  public ReportOffenseResponse reportPlayerOffense(
      @ToolParam(description = "玩家说了什么") String reason,
      @ToolParam(description = "冒犯程度 1~5（1=轻微冒犯，5=严重冒犯）") int severity) {
    try {
      Long userId = getCurrentUserId();
      var fudi =
          fudiService
              .findAndTouchFudi(userId)
              .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND));

      var spirit =
          spiritRepository
              .findByFudiId(fudi.getId())
              .orElseThrow(() -> new BusinessException(ErrorCode.SPIRIT_NOT_FOUND));

      int clampedSeverity = Math.clamp(severity, 1, 5);
      int oldAffection = spirit.getAffection();
      spirit.addAffection(-clampedSeverity);
      spiritRepository.save(spirit);

      log.info(
          "地灵冒犯上报 - userId: {}, reason: {}, severity: {}, affection: {} -> {}",
          userId,
          reason,
          clampedSeverity,
          oldAffection,
          spirit.getAffection());

      return new ReportOffenseResponse(true, reason, clampedSeverity);
    } catch (Exception e) {
      log.error("冒犯上报失败: reason={}", reason, e);
      return new ReportOffenseResponse(false, reason, 0);
    }
  }

  /** 获取当前用户 ID（从 ThreadLocal 上下文获取） */
  private Long getCurrentUserId() {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
      throw new BusinessException(ErrorCode.USER_CONTEXT_MISSING);
    }
    return userId;
  }

  // ===================== 响应 Record 定义 =====================

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

  public record ReportOffenseResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("冒犯原因") String reason,
      @JsonPropertyDescription("冒犯程度 1~5") int severity) {}
}
