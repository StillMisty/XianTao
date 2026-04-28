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
        return switch (fudiService.plantCropByInput(platform, openId, position, cropName)) {
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
                yield String.format("✅ 已收获地块 %s 的%s，获得 %d 份。", position, cropName, yield);
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
            case ServiceResult.Success(_) -> String.format("✅ 已在地块 %s 建造%s。", position, cellTypeName);
        };
    }

    public String handleRemove(PlatformType platform, String openId, String position) {
        return switch (fudiService.removeCell(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String typeName = (String) result.get("type");
                yield String.format("✅ 已拆除地块 %s 的%s。", position, typeName);
            }
        };
    }

    public String handleSacrifice(PlatformType platform, String openId, String input) {
        if ("all".equalsIgnoreCase(input)) {
            return "⚠️ 批量献祭功能尚未实现。";
        }
        return switch (fudiService.sacrificeItemByInput(platform, openId, input)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var auraGain) -> String.format("✅ 献祭成功！获得 %d 灵气。", auraGain);
        };
    }

    public String handleUpgradeCell(PlatformType platform, String openId, String position) {
        return switch (fudiService.upgradeCell(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String typeName = (String) result.get("type");
                int oldLevel = (Integer) result.get("oldLevel");
                int newLevel = (Integer) result.get("newLevel");
                yield String.format("✅ 已将地块 %s 的%s从 Lv%d 升级至 Lv%d", position, typeName, oldLevel, newLevel);
            }
        };
    }

    public String handleHatch(PlatformType platform, String openId, String position, String eggName) {
        return switch (fudiService.hatchBeastByInput(platform, openId, position, eggName)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var vo) -> formatHatchResult(vo);
        };
    }

    public String handleCollect(PlatformType platform, String openId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return switch (fudiService.collectAllProduce(platform, openId)) {
                case ServiceResult.Failure(var msg) -> "❌ " + msg;
                case ServiceResult.Success(var result) -> {
                    int positions = (Integer) result.get("totalPositions");
                    int items = (Integer) result.get("totalItems");
                    yield items == 0 ? "📦 没有可收取的灵兽产出。" :
                            String.format("✅ 从 %d 个兽栏收取了 %d 件灵兽材料。", positions, items);
                }
            };
        }
        return switch (fudiService.collectProduce(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var vo) ->
                    String.format("✅ 从地块 %s 收取了 %d 件%s。", vo.getCellId(), vo.getTotalProduced(), vo.getItemName());
        };
    }

    public String handleRelease(PlatformType platform, String openId, String position) {
        return switch (fudiService.releaseBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String beastName = (String) result.get("beastName");
                int essenceValue = (Integer) result.get("essenceValue");
                yield String.format("✅ 已放生%s，获得灵兽精华（价值 %d 灵气）。", beastName, essenceValue);
            }
        };
    }

    public String handleEvolve(PlatformType platform, String openId, String position, String mode) {
        return switch (fudiService.evolveBeast(platform, openId, position, mode)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var vo) -> formatEvolveResult(vo, mode);
        };
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

        // 天劫刚结算时，前置显示结果
        if (status.getTribulationResult() != null) {
            sb.append(status.getTribulationResult()).append("\n\n");
        }

        sb.append("🏔️ 【福地状态】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("🔮 灵气：").append(status.getAuraCurrent()).append("/").append(status.getAuraMax()).append("\n");
        sb.append("⚡ 每小时消耗：").append(status.getAuraHourlyCost()).append(" 灵气\n");
        sb.append("⛈️ 劫数：").append(status.getTribulationStage()).append("  连胜×").append(status.getTribulationWinStreak()).append("\n");
        sb.append("🎭 地灵人格：").append(status.getMbtiType().getCode()).append("\n");
        sb.append("😊 地灵情绪：").append(status.getEmotionState().getEmoji()).append(" ").append(status.getEmotionState().getDescription()).append("\n");
        sb.append("⚙️ 自动管理：").append(status.getAutoMode() ? "✅ 开启" : "❌ 关闭").append("\n");
        sb.append("📐 地块总数：").append(status.getTotalCells()).append("\n");
        sb.append("🏗️ 已占地块：").append(status.getOccupiedCells()).append("/").append(status.getTotalCells()).append("\n");
        if (Boolean.TRUE.equals(status.getIsAuraDepleted())) {
            sb.append("⚠️ 灵气已耗尽！除献祭外其他功能不可用\n");
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
                sb.append("📍 #").append(cell.getCellId()).append(" ");
                sb.append(cell.getType().getChineseName());
                if (cell.getCellLevel() != null && cell.getCellLevel() > 1) {
                    sb.append(" Lv").append(cell.getCellLevel());
                }
                if (cell.getName() != null) sb.append(" - ").append(cell.getName());
                if (cell.getGrowthProgress() != null) {
                    int percent = (int) (cell.getGrowthProgress() * 100);
                    sb.append(" [").append(percent).append("%]");
                    if (Boolean.TRUE.equals(cell.getIsMature())) sb.append(" ✅ 可收获");
                }
                if (cell.getQuality() != null) sb.append(" ").append(cell.getQuality());
                if (cell.getProductionStored() != null && cell.getProductionStored() > 0)
                    sb.append(" 📦x").append(cell.getProductionStored());
                if (Boolean.TRUE.equals(cell.getIsIncubating())) sb.append(" 🥚孵化中");
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
        if (status.getIsAuraDepleted()) {
            sb.append("⚠️ 灵气已耗尽！除献祭外其他功能不可用\n");
        }
        sb.append("⛈️ 劫数：Lv.").append(status.getTribulationStage()).append("\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 使用 #献祭 装备可补充灵气");
        return sb.toString();
    }

    private String formatSpiritInfo(FudiStatusVO status) {
        return "🧚 【地灵信息】\n" +
                "━━━━━━━━━━━━━━━\n" +
                "MBTI人格：" + status.getMbtiType().getCode() + "\n" +
                "语气风格：" + status.getMbtiType().getToneStyle() + "\n" +
                "劫数：" + status.getTribulationStage() + "\n" +
                "好感度：" + status.getSpiritAffection() + " 点\n" +
                "精力值：" + status.getSpiritEnergy() + "/100\n" +
                "当前情绪：" + status.getEmotionState().getEmoji() + " " + status.getEmotionState().getDescription() + "\n" +
                "━━━━━━━━━━━━━━━\n" +
                "💡 与地灵互动可提升好感度";
    }

    private String formatPlantResult(FarmCellVO result) {
        return String.format(
                "✅ 已在地块 %s 种植%s，预计 %.1f 小时后成熟。",
                result.getCellId(), result.getCropName(), result.getActualGrowthHours()
        );
    }

    private String formatHatchResult(top.stillmisty.xiantao.domain.land.vo.PenCellVO result) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("✅ 已开始在地块 %s 孵化%s！\n", result.getCellId(), result.getBeastName()));
        sb.append(String.format("品质：%s | 等阶：T%d\n", result.getQuality(), result.getTier()));
        if (result.isMutant()) {
            sb.append("✨ 变异灵兽！特性：").append(String.join(",", result.getMutationTraits())).append("\n");
        }
        sb.append(String.format(
                "预计 %.1f 小时后孵化完成。",
                java.time.Duration.between(java.time.LocalDateTime.now(), result.getMatureTime()).toHours()
        ));
        return sb.toString();
    }

    private String formatEvolveResult(top.stillmisty.xiantao.domain.land.vo.PenCellVO result, String mode) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("✅ %s成功！\n", mode));
        sb.append(String.format("灵兽：%s | 等阶：T%d | 品质：%s\n", result.getBeastName(), result.getTier(), result.getQuality()));
        if (result.isMutant()) {
            sb.append("能力：").append(String.join(",", result.getMutationTraits())).append("\n");
        }
        return sb.toString();
    }
}
