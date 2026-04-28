package top.stillmisty.xiantao.service;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.vo.BeastProduceVO;
import top.stillmisty.xiantao.domain.land.vo.FarmCellVO;

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
    private final ItemService itemService;

    /**
     * 查询福地网格状态工具
     */
    @Tool(description = "查询福地的网格布局状态，包括哪些地块编号是空的、哪些已被占用。在种植或建造前应该先调用此工具了解可用地块编号。")
    public GetGridStatusResponse getGridStatus() {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = fudiService.getGridStatus(userId);

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

            return new GetGridStatusResponse(true, message, totalCells, occupiedCount, emptyCount, emptyCellIds);
        } catch (Exception e) {
            log.error("查询网格状态失败", e);
            return new GetGridStatusResponse(false, "查询失败：" + e.getMessage(), 0, 0, 0, java.util.List.of());
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
                    var list = itemService.getSeedInventory(userId);
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
                    var list = itemService.getEquipmentInventory(userId);
                    if (list.isEmpty()) yield "背包中没有可装备的物品。";
                    var sb = new StringBuilder("【装备列表】\n");
                    for (var e : list) {
                        sb.append(e.index()).append(". ").append(e.name());
                        sb.append(" [").append(e.metadata()).append("]\n");
                    }
                    yield sb.toString().strip();
                }
                case "兽卵" -> {
                    var list = itemService.getEggInventory(userId);
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

            FarmCellVO result = fudiService.plantCropByInput(userId, position, cropName);

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
     * 收获灵药工具
     */
    @Tool(description = "收获福地中指定地块编号的成熟灵药。编号可以是具体数字或'all'表示全部收获。")
    public HarvestCropResponse harvestCrop(
            @ToolParam(description = "收获地块编号，如'1'、'2'，或'all'表示全部收获") String position
    ) {
        try {
            Long userId = getCurrentUserId();

            if ("all".equalsIgnoreCase(position)) {
                Map<String, Object> result = fudiService.harvestAllCrops(userId);
                int harvested = (Integer) result.get("harvested");
                int totalYield = (Integer) result.get("totalYield");

                String message;
                if (harvested == 0) {
                    message = "没有可收获的灵田。";
                } else {
                    message = String.format("已收获 %d 块灵田，共获得 %d 份素材。", harvested, totalYield);
                }

                return new HarvestCropResponse(true, message, position, harvested);
            } else {
                Map<String, Object> result = fudiService.harvestCrop(userId, position);
                String cropName = (String) result.get("cropName");
                int yield = (Integer) result.get("yield");

                return new HarvestCropResponse(
                        true,
                        String.format("已收获地块 %s 的%s，获得 %d 份。", position, cropName, yield),
                        position,
                        yield
                );
            }
        } catch (Exception e) {
            log.error("收获灵药失败: position={}", position, e);
            return new HarvestCropResponse(false, "收获失败：" + e.getMessage(), position, 0);
        }
    }

    /**
     * 建造地块工具
     */
    @Tool(description = "在福地的指定地块编号建造地块，可以建造灵田、兽栏或阵眼。")
    public BuildCellResponse buildCell(
            @ToolParam(description = "建造地块编号，如'1'、'2'等") String position,
            @ToolParam(description = "地块类型：灵田、兽栏、阵眼") String cellType
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
     * 献祭物品工具
     */
    @Tool(description = "献祭背包中的装备换取灵气。先调用queryBag('装备')查看可用装备的编号。可以指定装备编号、名称、品质(白色/绿色/蓝色/紫色/金色)或'all'。")
    public SacrificeItemResponse sacrificeItem(
            @ToolParam(description = "装备编号或名称，例如'1'、'2'，或装备名称。'all'表示献祭全部装备，品质名称(如'绿色')表示献祭指定品质的所有装备") String itemName
    ) {
        try {
            Long userId = getCurrentUserId();

            if ("all".equalsIgnoreCase(itemName)) {
                Map<String, Integer> result = fudiService.sacrificeAllItems(userId);
                return new SacrificeItemResponse(
                        true,
                        String.format("批量献祭完成！共献祭 %d 件装备，获得 %d 灵气。", result.get("count"), result.get("totalAura")),
                        "all",
                        result.get("totalAura")
                );
            }

            if (isQualityKeyword(itemName)) {
                Map<String, Integer> result = fudiService.sacrificeItemsByQuality(userId, itemName);
                return new SacrificeItemResponse(
                        true,
                        String.format("献祭完成！共献祭 %d 件%s品质装备，获得 %d 灵气。", result.get("count"), itemName, result.get("totalAura")),
                        itemName,
                        result.get("totalAura")
                );
            }

            int auraGain = fudiService.sacrificeItemByInput(userId, itemName);

            return new SacrificeItemResponse(
                    true,
                    String.format("献祭成功！%s 转化为 %d 灵气。", itemName, auraGain),
                    itemName,
                    auraGain
            );
        } catch (Exception e) {
            log.error("献祭物品失败: itemName={}", itemName, e);
            return new SacrificeItemResponse(false, "献祭失败：" + e.getMessage(), itemName, 0);
        }
    }

    private boolean isQualityKeyword(String input) {
        return switch (input) {
            case "白色", "绿色", "蓝色", "紫色", "金色",
                 "破旧", "普通", "稀有", "史诗", "传说" -> true;
            default -> false;
        };
    }

    /**
     * 收取灵兽产出工具
     */
    @Tool(description = "收取福地中指定兽栏的灵兽产出物。编号可以是具体数字或'all'表示全部收取。")
    public CollectProduceResponse collectProduce(
            @ToolParam(description = "收取地块编号，如'1'、'2'，或'all'表示全部收取") String position
    ) {
        try {
            Long userId = getCurrentUserId();

            if ("all".equalsIgnoreCase(position)) {
                Map<String, Object> result = fudiService.collectAllProduce(userId);
                int positions = (Integer) result.get("totalPositions");
                int items = (Integer) result.get("totalItems");

                String message;
                if (items == 0) {
                    message = "现在没有可收取的灵兽产出物。";
                } else {
                    message = String.format("已从 %d 个兽栏收取了 %d 件灵兽材料。", positions, items);
                }

                return new CollectProduceResponse(true, message, position, items);
            } else {
                BeastProduceVO result = fudiService.collectProduce(userId, position);

                return new CollectProduceResponse(
                        true,
                        String.format("已从地块 %s 收取了 %d 件%s。", position, result.getTotalProduced(), result.getItemName()),
                        position,
                        result.getTotalProduced()
                );
            }
        } catch (Exception e) {
            log.error("收取灵兽产出失败: position={}", position, e);
            return new CollectProduceResponse(false, "收取失败：" + e.getMessage(), position, 0);
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

    public record GetGridStatusResponse(
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

    public record SacrificeItemResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,

            @JsonPropertyDescription("结果消息")
            String message,

            @JsonPropertyDescription("物品名称")
            String itemName,

            @JsonPropertyDescription("获得的灵气数量")
            int auraGain
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
}
