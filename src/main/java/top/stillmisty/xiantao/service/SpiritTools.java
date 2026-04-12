package top.stillmisty.xiantao.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.enums.WuxingType;
import top.stillmisty.xiantao.domain.land.vo.FarmCellVO;

import java.util.Map;
import java.util.function.Function;

/**
 * 地灵可用的工具函数（Function Calling）
 * 这些工具会被 LLM 调用，以执行福地相关的操作
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritTools {

    private final FudiService fudiService;

    /**
     * 查询福地网格状态工具
     */
    @Tool(description = "查询福地的网格布局状态，包括哪些坐标是空的、哪些已被占用。在种植或建造前应该先调用此工具了解可用坐标。")
    public GetGridStatusResponse getGridStatus() {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = fudiService.getGridStatus(userId);
            
            int gridSize = (Integer) result.get("gridSize");
            int occupiedCount = (Integer) result.get("occupiedCount");
            int emptyCount = (Integer) result.get("emptyCount");
            @SuppressWarnings("unchecked")
            java.util.List<String> emptyPositions = (java.util.List<String>) result.get("emptyPositions");
            
            String message;
            if (emptyCount == 0) {
                message = String.format("福地已满（%dx%d网格，%d个地块全部占用）。", gridSize, gridSize, occupiedCount);
            } else {
                message = String.format("福地网格 %dx%d，已占用 %d 个地块，还有 %d 个空位。可用坐标：%s", 
                        gridSize, gridSize, occupiedCount, emptyCount, String.join(", ", emptyPositions));
            }
            
            return new GetGridStatusResponse(true, message, gridSize, occupiedCount, emptyCount, emptyPositions);
        } catch (Exception e) {
            log.error("查询网格状态失败", e);
            return new GetGridStatusResponse(false, "查询失败：" + e.getMessage(), 0, 0, 0, java.util.List.of());
        }
    }

    /**
     * 种植灵药工具
     */
    @Tool(description = "在福地的指定坐标种植灵药。需要先调用getGridStatus确认坐标为空。需要提供坐标（格式如'0,0'）和作物名称。")
    public PlantCropResponse plantCrop(
            @ToolParam(description = "种植坐标，格式如'0,0'、'1,2'等") String position,
            @ToolParam(description = "作物名称，如'灵芝'、'人参'、'火莲'等") String cropName
    ) {
        try {
            // TODO: 从用户上下文获取 userId
            Long userId = getCurrentUserId();
            
            // TODO: 根据作物名称查询真实的 cropId 和 element
            WuxingType element = WuxingType.WOOD;
            Integer cropId = 101;

            FarmCellVO result = fudiService.plantCrop(userId, position, cropId, cropName, element);
            
            return new PlantCropResponse(
                    true,
                    String.format("已在 (%s) 种植%s，预计 %.1f 小时后成熟。", 
                            position, cropName, result.getBaseGrowthHours() / result.getGrowthModifier()),
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
    @Tool(description = "收获福地中指定坐标的成熟灵药。坐标可以是具体坐标或'all'表示全部收获。")
    public HarvestCropResponse harvestCrop(
            @ToolParam(description = "收获坐标，格式如'0,0'，或'all'表示全部收获") String position
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
                        String.format("已收获 (%s) 的%s，获得 %d 份。", position, cropName, yield),
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
    @Tool(description = "在福地的指定坐标建造地块，可以建造灵田、兽栏或阵眼。")
    public BuildCellResponse buildCell(
            @ToolParam(description = "建造坐标，格式如'0,0'") String position,
            @ToolParam(description = "地块类型：灵田、兽栏、阵眼") String cellType
    ) {
        try {
            Long userId = getCurrentUserId();
            CellType type = CellType.fromChineseName(cellType);
            
            Map<String, Object> result = fudiService.buildCell(userId, position, type);
            
            return new BuildCellResponse(
                    true,
                    String.format("已在 (%s) 建造%s。", position, cellType),
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
    @Tool(description = "拆除福地中指定坐标的地块。")
    public RemoveCellResponse removeCell(
            @ToolParam(description = "拆除坐标，格式如'0,0'") String position
    ) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = fudiService.removeCell(userId, position);
            String typeName = (String) result.get("type");
            
            return new RemoveCellResponse(
                    true,
                    String.format("已拆除 (%s) 的%s。", position, typeName),
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
    @Tool(description = "献祭背包中的装备换取灵气。可以指定物品名称或'all'表示献祭所有白色品质装备。")
    public SacrificeItemResponse sacrificeItem(
            @ToolParam(description = "物品名称，或'all'表示批量献祭") String itemName
    ) {
        try {
            Long userId = getCurrentUserId();
            
            if ("all".equalsIgnoreCase(itemName)) {
                // TODO: 实现批量献祭
                return new SacrificeItemResponse(false, "批量献祭功能尚未实现。", itemName, 0);
            }
            
            // TODO: 根据物品名称查找真实 ID
            Long itemId = 1L;
            int auraGain = fudiService.sacrificeItem(userId, itemId);
            
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

    /**
     * 喂养灵兽工具
     */
    @Tool(description = "喂养福地中指定兽栏的灵兽。需要提供坐标和饲料名称。")
    public FeedBeastResponse feedBeast(
            @ToolParam(description = "兽栏坐标，格式如'0,0'") String position,
            @ToolParam(description = "饲料名称") String feedName
    ) {
        try {
            Long userId = getCurrentUserId();
            
            // TODO: 根据饲料名称查找真实 ID
            Integer feedItemId = 1;
            
            Map<String, Object> result = fudiService.feedBeast(userId, position, feedItemId, feedName);
            String beastName = (String) result.get("beastName");
            int newHunger = (Integer) result.get("newHunger");
            
            return new FeedBeastResponse(
                    true,
                    String.format("已喂养 (%s) 的%s，当前饥饿值：%d", position, beastName, newHunger),
                    position,
                    beastName
            );
        } catch (Exception e) {
            log.error("喂养灵兽失败: position={}, feedName={}", position, feedName, e);
            return new FeedBeastResponse(false, "喂养失败：" + e.getMessage(), position, null);
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
            
            @JsonPropertyDescription("网格大小")
            int gridSize,
            
            @JsonPropertyDescription("已占地块数")
            int occupiedCount,
            
            @JsonPropertyDescription("空位数")
            int emptyCount,
            
            @JsonPropertyDescription("可用坐标列表")
            java.util.List<String> emptyPositions
    ) {}

    public record PlantCropResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,
            
            @JsonPropertyDescription("结果消息")
            String message,
            
            @JsonPropertyDescription("种植坐标")
            String position,
            
            @JsonPropertyDescription("作物名称")
            String cropName
    ) {}

    public record HarvestCropResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,
            
            @JsonPropertyDescription("结果消息")
            String message,
            
            @JsonPropertyDescription("收获坐标")
            String position,
            
            @JsonPropertyDescription("收获数量")
            int yield
    ) {}

    public record BuildCellResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,
            
            @JsonPropertyDescription("结果消息")
            String message,
            
            @JsonPropertyDescription("建造坐标")
            String position,
            
            @JsonPropertyDescription("地块类型")
            String cellType
    ) {}

    public record RemoveCellResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,
            
            @JsonPropertyDescription("结果消息")
            String message,
            
            @JsonPropertyDescription("拆除坐标")
            String position
    ) {}

    public record SacrificeItemResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,
            
            @JsonPropertyDescription("结果消息")
            String message,
            
            @JsonPropertyDescription("物品名称")
            String itemName,
            
            @JsonPropertyDescription("获得的灵气数量")
            int auraGain
    ) {}

    public record FeedBeastResponse(
            @JsonPropertyDescription("是否成功")
            boolean success,
            
            @JsonPropertyDescription("结果消息")
            String message,
            
            @JsonPropertyDescription("兽栏坐标")
            String position,
            
            @JsonPropertyDescription("灵兽名称")
            String beastName
    ) {}
}
