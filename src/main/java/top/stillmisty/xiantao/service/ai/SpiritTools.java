package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.fudi.enums.CellType;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.fudi.vo.FarmCellVO;
import top.stillmisty.xiantao.service.*;

import java.util.Map;

/**
 * 地灵可用的工具函数（Function Calling）
 * 这些工具会被 LLM 调用，以执行福地相关的操作
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritTools {

    private final FudiService fudiService;
    private final BeastService beastService;
    private final FarmService farmService;
    private final InventoryService inventoryService;
    private final FudiRepository fudiRepository;
    private final SpiritRepository spiritRepository;

    /**
     * 查询福地地块状态工具
     */
    @Tool(description = "查询福地的地块布局状态，包括哪些地块编号是空的、哪些已被占用。在种植或建造前应该先调用此工具了解可用地块编号。")
    public GetCellStatusResponse getCellStatus() {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = fudiService.getCellStatus(userId);

            int totalCells = (Integer) result.get("totalCells");
            int occupiedCount = (Integer) result.get("occupiedCount");
            int emptyCount = (Integer) result.get("emptyCount");
            @SuppressWarnings("unchecked")
            java.util.List<Integer> emptyCellIds = (java.util.List<Integer>) result.get("emptyCellIds");

            String message;
            if (emptyCount == 0) {
                message = String.format("福地已满（共%d个地块，全部占用）。", totalCells);
            } else {
                message = String.format(
                        "福地共 %d 个地块，已占用 %d 个，还有 %d 个空位。可用地块编号：%s",
                        totalCells, occupiedCount, emptyCount, emptyCellIds.toString()
                );
            }

            return new GetCellStatusResponse(true, message, totalCells, occupiedCount, emptyCount, emptyCellIds);
        } catch (Exception e) {
            log.error("查询地块状态失败", e);
            return new GetCellStatusResponse(false, "查询失败：" + e.getMessage(), 0, 0, 0, java.util.List.of());
        }
    }

    /**
     * 查询玩家背包工具
     */
    @Tool(description = "查询玩家背包中的物品列表。可以查看种子、装备或兽卵。返回编号列表，后续可以使用编号代替名称来操作物品。")
    public String queryBag(
            @ToolParam(description = "要查询的类别：种子、装备、兽卵") String category
    ) {
        try {
            Long userId = getCurrentUserId();
            var response = switch (category) {
                case "种子" -> {
                    var list = inventoryService.getSeedInventory(userId);
                    if (list.isEmpty()) yield "背包中没有种子。";
                    var sb = new StringBuilder("【种子列表】\n");
                    for (var e : list) {
                        sb.append(e.index()).append(". ").append(e.name());
                        if (e.quantity() > 1) sb.append(" x").append(e.quantity());
                        sb.append(" [").append(e.metadata()).append("]\n");
                    }
                    yield sb.toString().strip();
                }
                case "装备" -> {
                    var list = inventoryService.getEquipmentInventory(userId);
                    if (list.isEmpty()) yield "背包中没有可装备的物品。";
                    var sb = new StringBuilder("【装备列表】\n");
                    for (var e : list) {
                        sb.append(e.index()).append(". ").append(e.name());
                        sb.append(" [").append(e.metadata()).append("]\n");
                    }
                    yield sb.toString().strip();
                }
                case "兽卵" -> {
                    var list = inventoryService.getEggInventory(userId);
                    if (list.isEmpty()) yield "背包中没有兽卵。";
                    var sb = new StringBuilder("【兽卵列表】\n");
                    for (var e : list) {
                        sb.append(e.index()).append(". ").append(e.name());
                        if (e.quantity() > 1) sb.append(" x").append(e.quantity());
                        sb.append(" [").append(e.metadata()).append("]\n");
                    }
                    yield sb.toString().strip();
                }
                default -> "支持的类别：种子、装备、兽卵";
            };
            return response;
        } catch (Exception e) {
            log.error("查询背包失败: category={}", category, e);
            return "查询背包失败：" + e.getMessage();
        }
    }

    /**
     * 种植灵药工具
     */
    @Tool(description = "在福地的指定地块编号种植灵药。需要先调用getGridStatus确认地块为空，再调用queryBag('种子')查看可用种子的编号。提供地块编号（如1、2等）和种子名称或编号。")
    public PlantCropResponse plantCrop(
            @ToolParam(description = "种植地块编号，如'1'、'2'等") String position,
            @ToolParam(description = "种子编号或名称。先调用queryBag('种子')查看编号，例如'1'、'2'，或直接使用名称如'灵草种子'") String cropName
    ) {
        try {
            Long userId = getCurrentUserId();

            FarmCellVO result = farmService.plantCropByInput(userId, position, cropName);

            return new PlantCropResponse(
                    true,
                    String.format(
                            "已在地块 %s 种植%s，预计 %.1f 小时后成熟。",
                            position, cropName, result.getBaseGrowthHours()
                    ),
                    position,
                    cropName
            );
        } catch (Exception e) {
            log.error("种植灵药失败: position={}, cropName={}", position, cropName, e);
            return new PlantCropResponse(false, "种植失败：" + e.getMessage(), position, cropName);
        }
    }

    /**
     * 收取地块产出的统一工具（灵田收获 + 兽栏收取）
     */
    @Tool(description = "收取福地中指定地块编号的产出物（自动判断灵田收获或兽栏收取）。编号可以是具体数字或'all'表示全部收取。")
    public HarvestCropResponse harvestCrop(
            @ToolParam(description = "收取地块编号，如'1'、'2'，或'all'表示全部收取") String position
    ) {
        try {
            Long userId = getCurrentUserId();

            if ("all".equalsIgnoreCase(position)) {
                Map<String, Object> result = fudiService.collectAll(userId);
                int harvested = (Integer) result.getOrDefault("harvested", 0);
                int collected = (Integer) result.getOrDefault("collected", 0);
                int totalItems = (Integer) result.getOrDefault("totalItems", 0);

                if (totalItems == 0) {
                    return new HarvestCropResponse(true, "没有可收取的内容。", position, 0);
                }
                var parts = new java.util.ArrayList<String>();
                if (harvested > 0) parts.add("收获 %d 块灵田".formatted(harvested));
                if (collected > 0) parts.add("收取 %d 个兽栏".formatted(collected));
                String message = "已" + String.join("、", parts) + "，共获得 " + totalItems + " 份物资。";

                return new HarvestCropResponse(true, message, position, totalItems);
            } else {
                Map<String, Object> result = fudiService.collect(userId, position);
                String type = (String) result.get("type");
                int yield = 0;
                String message;
                if ("farm".equals(type)) {
                    String cropName = (String) result.get("cropName");
                    yield = (Integer) result.get("yield");
                    message = String.format("已收获地块 %s 的%s，获得 %d 份。", position, cropName, yield);
                } else {
                    String beastName = (String) result.get("beastName");
                    yield = (Integer) result.get("totalItems");
                    message = String.format("已从地块 %s 收取了 %d 件%s产出。", position, yield, beastName);
                }

                return new HarvestCropResponse(true, message, position, yield);
            }
        } catch (Exception e) {
            log.error("收取产出失败: position={}", position, e);
            return new HarvestCropResponse(false, "收取失败：" + e.getMessage(), position, 0);
        }
    }

    /**
     * 建造地块工具
     */
    @Tool(description = "在福地的指定地块编号建造地块，可以建造灵田或兽栏。")
    public BuildCellResponse buildCell(
            @ToolParam(description = "建造地块编号，如'1'、'2'等") String position,
            @ToolParam(description = "地块类型：灵田、兽栏") String cellType
    ) {
        try {
            Long userId = getCurrentUserId();
            CellType type = CellType.fromChineseName(cellType);

            Map<String, Object> result = fudiService.buildCell(userId, position, type);

            return new BuildCellResponse(
                    true,
                    String.format("已在地块 %s 建造%s。", position, cellType),
                    position,
                    cellType
            );
        } catch (Exception e) {
            log.error("建造地块失败: position={}, cellType={}", position, cellType, e);
            return new BuildCellResponse(false, "建造失败：" + e.getMessage(), position, cellType);
        }
    }

    /**
     * 拆除地块工具
     */
    @Tool(description = "拆除福地中指定地块编号的建筑。")
    public RemoveCellResponse removeCell(
            @ToolParam(description = "拆除地块编号，如'1'、'2'等") String position
    ) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = fudiService.removeCell(userId, position);
            String typeName = (String) result.get("type");

            return new RemoveCellResponse(
                    true,
                    String.format("已拆除地块 %s 的%s。", position, typeName),
                    position
            );
        } catch (Exception e) {
            log.error("拆除地块失败: position={}", position, e);
            return new RemoveCellResponse(false, "拆除失败：" + e.getMessage(), position);
        }
    }

    /**
     * 收取灵兽产出工具
     */
    @Tool(description = "收取福地中指定地块的产出物（统一收取灵田作物和兽栏产出）。编号可以是具体数字或'all'表示全部收取。")
    public CollectProduceResponse collectProduce(
            @ToolParam(description = "收取地块编号，如'1'、'2'，或'all'表示全部收取") String position
    ) {
        try {
            Long userId = getCurrentUserId();

            if ("all".equalsIgnoreCase(position)) {
                Map<String, Object> result = fudiService.collectAll(userId);
                int harvested = (Integer) result.getOrDefault("harvested", 0);
                int collected = (Integer) result.getOrDefault("collected", 0);
                int totalItems = (Integer) result.getOrDefault("totalItems", 0);

                if (totalItems == 0) {
                    return new CollectProduceResponse(true, "现在没有可收取的内容。", position, 0);
                }
                return new CollectProduceResponse(
                        true,
                        String.format("已收获 %d 块灵田、收取 %d 个兽栏，共获得 %d 份物资。", harvested, collected, totalItems),
                        position,
                        totalItems
                );
            } else {
                Map<String, Object> result = fudiService.collect(userId, position);
                String type = (String) result.get("type");
                int items;
                String message;
                if ("farm".equals(type)) {
                    String cropName = (String) result.get("cropName");
                    items = (Integer) result.get("yield");
                    message = String.format("已收获地块 %s 的%s，获得 %d 份。", position, cropName, items);
                } else {
                    String beastName = (String) result.get("beastName");
                    items = (Integer) result.get("totalItems");
                    message = String.format("已从地块 %s 收取了 %d 件%s产出。", position, items, beastName);
                }

                return new CollectProduceResponse(true, message, position, items);
            }
        } catch (Exception e) {
            log.error("收取产出失败: position={}", position, e);
            return new CollectProduceResponse(false, "收取失败：" + e.getMessage(), position, 0);
        }
    }

    /**
     * 赠送礼物工具
     */
    @Tool(description = "赠送背包中的物品给地灵。当地灵表示喜欢某物或玩家主动送礼时调用。参数为物品名称。")
    public GiveGiftResponse giveGift(
            @ToolParam(description = "物品名称，如'精铁剑'、'灵石'等") String itemName
    ) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = fudiService.giveGift(userId, itemName);

            String reaction = (String) result.get("reaction");
            int change = (Integer) result.get("change");
            String giftName = (String) result.get("itemName");
            boolean isLiked = Boolean.TRUE.equals(result.get("isLiked"));
            boolean isDisliked = Boolean.TRUE.equals(result.get("isDisliked"));

            String message;
            if (isLiked) {
                message = String.format("地灵收到%s，非常开心！好感度 %+d", giftName, change);
            } else if (isDisliked) {
                message = String.format("地灵嫌弃地看了一眼%s，好感度 %d", giftName, change);
            } else {
                message = String.format("地灵礼貌地收下了%s，好感度 %+d", giftName, change);
            }

            return new GiveGiftResponse(true, message, giftName, change, reaction);
        } catch (Exception e) {
            log.error("赠送礼物失败: itemName={}", itemName, e);
            return new GiveGiftResponse(false, "送礼失败：" + e.getMessage(), itemName, 0, "");
        }
    }

    /**
     * 冒犯上报工具（由 LLM 主动调用）
     */
    @Tool(description = "当玩家说了不礼貌、冒犯、或让地灵不开心的话时调用此工具。由地灵自主判断是否需要调用。")
    public ReportOffenseResponse reportPlayerOffense(
            @ToolParam(description = "玩家说了什么") String reason,
            @ToolParam(description = "冒犯程度 1~5（1=轻微冒犯，5=严重冒犯）") int severity
    ) {
        try {
            Long userId = getCurrentUserId();
            var fudi = fudiService.getFudiByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("未找到福地"));

            var spirit = spiritRepository.findByFudiId(fudi.getId())
                    .orElseThrow(() -> new IllegalStateException("地灵不存在"));

            int clampedSeverity = Math.max(1, Math.min(5, severity));
            int oldAffection = spirit.getAffection();
            spirit.addAffection(-clampedSeverity);
            spiritRepository.save(spirit);

            log.info(
                    "地灵冒犯上报 - userId: {}, reason: {}, severity: {}, affection: {} -> {}",
                    userId, reason, clampedSeverity, oldAffection, spirit.getAffection()
            );

            return new ReportOffenseResponse(true, reason, clampedSeverity);
        } catch (Exception e) {
            log.error("冒犯上报失败: reason={}", reason, e);
            return new ReportOffenseResponse(false, reason, 0);
        }
    }

    /**
     * 获取当前用户 ID（从 ThreadLocal 上下文获取）
     */
    private Long getCurrentUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("未找到用户上下文，请先设置 UserContext");
        }
        return userId;
    }

    // ===================== 响应 Record 定义 =====================

    public record GetCellStatusResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("地块总数")
            int totalCells,

            @JsonPropertyDescription("已占地块数")
            int occupiedCount,

            @JsonPropertyDescription("空位数")
            int emptyCount,

            @JsonPropertyDescription("可用地块编号列表")
            java.util.List<Integer> emptyCellIds
    ) {
    }

    public record PlantCropResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("种植地块编号")
            String position,

            @JsonPropertyDescription("作物名称")
            String cropName
    ) {
    }

    public record HarvestCropResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("收获地块编号")
            String position,

            @JsonPropertyDescription("收获数量")
            int yield
    ) {
    }

    public record BuildCellResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("建造地块编号")
            String position,

            @JsonPropertyDescription("地块类型")
            String cellType
    ) {
    }

    public record RemoveCellResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("拆除地块编号")
            String position
    ) {
    }

    public record CollectProduceResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("收取地块编号")
            String position,

            @JsonPropertyDescription("收取数量")
            int collected
    ) {
    }

    public record GiveGiftResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("物品名称")
            String itemName,

            @JsonPropertyDescription("好感度变化")
            int affectionChange,

            @JsonPropertyDescription("地灵反应")
            String reaction
    ) {
    }

    public record ReportOffenseResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("冒犯原因")
            String reason,

            @JsonPropertyDescription("冒犯程度 1~5")
            int severity
    ) {
    }
}
