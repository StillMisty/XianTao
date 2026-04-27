package top.stillmisty.xiantao.handle.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.land.vo.FudiStatusVO;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.FudiService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.SpiritChatService;

import java.util.Map;

/**
 * 福地命令处理器（纯 View 层）
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FudiCommandHandler {

    private final FudiService fudiService;
    private final SpiritChatService spiritChatService;

    public String handleFudiStatus(PlatformType platform, String openId) {
        return switch (fudiService.getFudiStatus(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatFudiStatus(vo);
        };
    }

    public String handleFudiGrid(PlatformType platform, String openId) {
        return switch (fudiService.getFudiStatus(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatGridLayout(vo);
        };
    }

    public String handleFudiAura(PlatformType platform, String openId) {
        return switch (fudiService.getFudiStatus(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatAuraInfo(vo);
        };
    }

    public String handleFudiSpirit(PlatformType platform, String openId) {
        return switch (fudiService.getFudiStatus(platform, openId)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var vo) -> formatSpiritInfo(vo);
        };
    }

    public String handlePlant(PlatformType platform, String openId, String position, String cropName) {
        return switch (fudiService.plantCropByName(platform, openId, position, cropName)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var vo) -> formatPlantResult(vo);
        };
    }

    public String handleHarvest(PlatformType platform, String openId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return switch (fudiService.harvestAllCrops(platform, openId)) {
                case ServiceResult.Failure(var msg) -> "❌ " + msg;
                case ServiceResult.Success(var result) -> {
                    int harvested = (Integer) result.get("harvested");
                    int totalYield = (Integer) result.get("totalYield");
                    yield harvested == 0 ? "🌾 没有可收获的灵田。" :
                            String.format("✅ 已收获 %d 块灵田，共获得 %d 份素材。", harvested, totalYield);
                }
            };
        }
        return switch (fudiService.harvestCrop(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String cropName = (String) result.get("cropName");
                int yield = (Integer) result.get("yield");
                yield String.format("✅ 已收获 (%s) 的%s，获得 %d 份。", position, cropName, yield);
            }
        };
    }

    public String handleBuild(PlatformType platform, String openId, String position, String cellTypeName) {
        CellType cellType;
        try {
            cellType = CellType.fromChineseName(cellTypeName);
        } catch (IllegalArgumentException e) {
            return "❌ 不支持的地块类型：" + cellTypeName + "（可选：灵田、兽栏、阵眼）";
        }
        return switch (fudiService.buildCell(platform, openId, position, cellType)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(_) -> String.format("✅ 已在 (%s) 建造%s。", position, cellTypeName);
        };
    }

    public String handleRemove(PlatformType platform, String openId, String position) {
        return switch (fudiService.removeCell(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String typeName = (String) result.get("type");
                yield String.format("✅ 已拆除 (%s) 的%s。", position, typeName);
            }
        };
    }

    public String handleSacrifice(PlatformType platform, String openId, String itemName) {
        if ("all".equalsIgnoreCase(itemName)) {
            return "⚠️ 批量献祭功能尚未实现。";
        }
        return switch (fudiService.sacrificeItemByName(platform, openId, itemName)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var auraGain) ->
                    String.format("✅ 献祭成功！%s 转化为 %d 灵气。", itemName, auraGain);
        };
    }

    public String handleFeed(PlatformType platform, String openId, String position, String feedItemName) {
        return switch (fudiService.feedBeastByName(platform, openId, position, feedItemName)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> formatFeedResult(result);
        };
    }

    public String handleAutoMode(PlatformType platform, String openId, String mode) {
        return "⚠️ 福地自动管理模式切换功能尚未实现。";
    }

    public String handleUpgrade(PlatformType platform, String openId) {
        return "⚠️ 福地升级功能尚未实现。";
    }

    public String handleExpand(PlatformType platform, String openId) {
        return "⚠️ 福地扩建功能尚未实现。";
    }

    public String handleSpiritChat(PlatformType platform, String openId, String userInput) {
        log.info("处理地灵自然语言交互 - platform: {}, input: {}", platform, userInput);
        return switch (spiritChatService.chatWithSpirit(platform, openId, userInput)) {
            case ServiceResult.Failure(var msg) -> msg;
            case ServiceResult.Success(var response) -> response;
        };
    }

    // ===================== 文本格式化方法 =====================

    private String formatFudiStatus(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏔️ 【福地状态】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("🔮 灵气：").append(status.getAuraCurrent()).append("/").append(status.getAuraMax()).append("\n");
        sb.append("⚡ 每小时消耗：").append(status.getAuraHourlyCost()).append(" 灵气\n");
        sb.append("🏰 福地等级：Lv.").append(status.getCoreLevel()).append("\n");
        sb.append("🎭 地灵人格：").append(status.getMbtiType().getCode()).append("\n");
        sb.append("😊 地灵情绪：").append(status.getEmotionState().getEmoji()).append(" ").append(status.getEmotionState().getDescription()).append("\n");
        sb.append("⚙️ 自动管理：").append(status.getAutoMode() ? "✅ 开启" : "❌ 关闭").append("\n");
        sb.append("🛌 蛰伏模式：").append(status.getDormantMode() ? "⚠️ 激活" : "✅ 未激活").append("\n");
        sb.append("📐 网格大小：").append(status.getGridSize()).append("x").append(status.getGridSize()).append("\n");
        sb.append("🏗️ 已占地块：").append(status.getOccupiedCells()).append("/").append(status.getGridSize() * status.getGridSize()).append("\n");
        if (status.getScorchedCells() > 0) {
            sb.append("🔥 焦土地块：").append(status.getScorchedCells()).append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 使用 #福地网格 查看详细布局");
        return sb.toString();
    }

    private String formatGridLayout(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();
        sb.append("🗺️ 【福地网格布局】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        if (status.getCellDetails() == null || status.getCellDetails().isEmpty()) {
            sb.append("（空地，尚未建造任何地块）\n");
        } else {
            for (var cell : status.getCellDetails()) {
                sb.append("📍 (").append(cell.getPosition()).append(") ");
                sb.append(cell.getType().getChineseName());
                if (cell.getName() != null) sb.append(" - ").append(cell.getName());
                if (cell.getGrowthProgress() != null) {
                    int percent = (int) (cell.getGrowthProgress() * 100);
                    sb.append(" [").append(percent).append("%]");
                    if (Boolean.TRUE.equals(cell.getIsMature())) sb.append(" ✅ 可收获");
                }
                if (cell.getHunger() != null) sb.append(" 饥饿值:").append(cell.getHunger());
                if (cell.getDurability() != null) sb.append(" 耐久:").append(cell.getDurability());
                sb.append("\n");
            }
        }
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 使用 #种植/#建造/#收获 进行操作");
        return sb.toString();
    }

    private String formatAuraInfo(FudiStatusVO status) {
        int current = status.getAuraCurrent();
        int max = status.getAuraMax();
        int hourlyCost = status.getAuraHourlyCost();
        int percent = (int) ((double) current / max * 100);
        StringBuilder sb = new StringBuilder();
        sb.append("🔮 【灵气详情】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("当前灵气：").append(current).append("/").append(max).append(" (").append(percent).append("%)\n");
        sb.append("每小时消耗：").append(hourlyCost).append(" 灵气\n");
        if (hourlyCost > 0) {
            sb.append("预计可用：").append(current / hourlyCost).append(" 小时\n");
        }
        sb.append("聚灵核心等级：Lv.").append(status.getCoreLevel()).append("\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 使用 #献祭 装备可补充灵气");
        return sb.toString();
    }

    private String formatSpiritInfo(FudiStatusVO status) {
        return "🧚 【地灵信息】\n" +
                "━━━━━━━━━━━━━━━\n" +
                "MBTI人格：" + status.getMbtiType().getCode() + "\n" +
                "语气风格：" + status.getMbtiType().getToneStyle() + "\n" +
                "福地等级：Lv." + status.getCoreLevel() + "\n" +
                "好感度：" + status.getSpiritAffection() + " 点\n" +
                "精力值：" + status.getSpiritEnergy() + "/100\n" +
                "当前情绪：" + status.getEmotionState().getEmoji() + " " + status.getEmotionState().getDescription() + "\n" +
                "━━━━━━━━━━━━━━━\n" +
                "💡 与地灵互动可提升好感度";
    }

    private String formatPlantResult(FarmCellVO result) {
        return String.format(
                "✅ 已在 (%s) 种植%s，预计 %.1f 小时后成熟。\n五行属性：%s\n生长修正：%.0f%%",
                result.getPosition(), result.getCropName(), result.getActualGrowthHours(),
                result.getElement().getChineseName(), result.getGrowthModifier() * 100
        );
    }

    private String formatFeedResult(Map<String, Object> result) {
        String beastName = (String) result.get("beastName");
        int newHunger = (Integer) result.get("newHunger");
        String position = (String) result.get("position");
        return String.format("✅ 已喂养 (%s) 的%s，当前饥饿值：%d", position, beastName, newHunger);
    }
}
