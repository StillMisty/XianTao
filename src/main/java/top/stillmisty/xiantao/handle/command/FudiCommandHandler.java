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
import top.stillmisty.xiantao.service.ai.SpiritChatService;

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
            case ServiceResult.Success(var vo) -> formatCellLayout(vo);
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

    public String handleCollect(PlatformType platform, String openId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            return switch (fudiService.collectAll(platform, openId)) {
                case ServiceResult.Failure(var msg) -> "❌ " + msg;
                case ServiceResult.Success(var result) -> {
                    int harvested = (Integer) result.getOrDefault("harvested", 0);
                    int collected = (Integer) result.getOrDefault("collected", 0);
                    int totalItems = (Integer) result.getOrDefault("totalItems", 0);
                    if (totalItems == 0) {
                        yield "没有可收取的内容。";
                    }
                    var parts = new java.util.ArrayList<String>();
                    if (harvested > 0) parts.add("收获 %d 块灵田".formatted(harvested));
                    if (collected > 0) parts.add("收取 %d 个兽栏".formatted(collected));
                    yield "✅ 已" + String.join("、", parts) + "，共获得 " + totalItems + " 份物资。";
                }
            };
        }
        return switch (fudiService.collect(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String type = (String) result.get("type");
                if ("farm".equals(type)) {
                    String cropName = (String) result.get("cropName");
                    int yield = (Integer) result.get("yield");
                    yield "✅ 已收获地块 %s 的%s，获得 %d 份。".formatted(position, cropName, yield);
                } else {
                    String beastName = (String) result.get("beastName");
                    int items = (Integer) result.get("totalItems");
                    yield "✅ 从地块 %s 收取了 %d 件%s产出。".formatted(position, items, beastName);
                }
            }
        };
    }

    public String handleBuild(PlatformType platform, String openId, String position, String cellTypeName) {
        CellType cellType;
        try {
            cellType = CellType.fromChineseName(cellTypeName);
        } catch (IllegalArgumentException e) {
            return "❌ 不支持的地块类型：" + cellTypeName + "（可选：灵田、兽栏）";
        }
        return switch (fudiService.buildCell(platform, openId, position, cellType)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(_) -> "✅ 已在地块 %s 建造%s。".formatted(position, cellTypeName);
        };
    }

    public String handleRemove(PlatformType platform, String openId, String position) {
        return switch (fudiService.removeCell(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String typeName = (String) result.get("type");
                yield "✅ 已拆除地块 %s 的%s。".formatted(position, typeName);
            }
        };
    }

    public String handleUpgradeCell(PlatformType platform, String openId, String position) {
        return switch (fudiService.upgradeCell(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String typeName = (String) result.get("type");
                int oldLevel = (Integer) result.get("oldLevel");
                int newLevel = (Integer) result.get("newLevel");
                yield "✅ 已将地块 %s 的%s从 Lv%d 升级至 Lv%d".formatted(position, typeName, oldLevel, newLevel);
            }
        };
    }

    public String handleHatch(PlatformType platform, String openId, String position, String eggName) {
        return switch (fudiService.hatchBeastByInput(platform, openId, position, eggName)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var vo) -> formatHatchResult(vo);
        };
    }

    public String handleRelease(PlatformType platform, String openId, String position) {
        return switch (fudiService.releaseBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                String beastName = (String) result.get("beastName");
                yield "✅ 已放生%s。".formatted(beastName);
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

    public String handleGiveGift(PlatformType platform, String openId, String itemName) {
        return switch (fudiService.giveGift(platform, openId, itemName)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                int change = (Integer) result.get("change");
                String name = (String) result.get("itemName");
                String reaction = (String) result.get("reaction");
                String prefix = change > 0 ? "✅" : "😅";
                yield "%s 地灵收到了%s（%s），好感度 %+d".formatted(prefix, name, reaction, change);
            }
        };
    }

    public String handleDeployBeast(PlatformType platform, String openId, String position) {
        return switch (fudiService.deployBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> (String) result.getOrDefault("message", "出战完成");
        };
    }

    public String handleUndeployBeast(PlatformType platform, String openId, String position) {
        return switch (fudiService.undeployBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                if (result.containsKey("count")) {
                    int count = (Integer) result.get("count");
                    yield "✅ 已召回 %d 只灵兽。".formatted(count);
                }
                yield (String) result.getOrDefault("message", "召回完成");
            }
        };
    }

    public String handleRecoverBeast(PlatformType platform, String openId, String position) {
        return switch (fudiService.recoverBeast(platform, openId, position)) {
            case ServiceResult.Failure(var msg) -> "❌ " + msg;
            case ServiceResult.Success(var result) -> {
                if (result.containsKey("count")) {
                    int count = (Integer) result.get("count");
                    int cost = (Integer) result.get("cost");
                    yield "✅ 已恢复 %d 只灵兽，消耗 %d 灵石。".formatted(count, cost);
                }
                yield (String) result.getOrDefault("message", "恢复完成");
            }
        };
    }

    // ===================== 文本格式化方法 =====================

    private String formatFudiStatus(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();

        if (status.getTribulationResult() != null) {
            sb.append(status.getTribulationResult()).append("\n\n");
        }

        sb.append("🏔️ 【福地状态】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("⛈️ 劫数：").append(status.getTribulationStage()).append("  连胜×").append(status.getTribulationWinStreak()).append("\n");
        sb.append("🧚 地灵形态：").append(status.getSpiritFormName() != null ? status.getSpiritFormName() : "未知形态").append("\n");
        sb.append("🎭 地灵人格：").append(status.getMbtiType().getCode()).append("\n");
        sb.append("😊 地灵情绪：").append(status.getEmotionState().getEmoji()).append(" ").append(status.getEmotionState().getDescription()).append("\n");
        sb.append("📐 地块总数：").append(status.getTotalCells()).append("\n");
        sb.append("🏗️ 已占地块：").append(status.getOccupiedCells()).append("/").append(status.getTotalCells()).append("\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 输入「福地地块」查看详细布局");
        return sb.toString();
    }

    private String formatCellLayout(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();
        sb.append("🗺️ 【福地地块布局】\n");
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
                    if (Boolean.TRUE.equals(cell.getIsMature())) sb.append(" ✅ 可收取");
                }
                if (cell.getQuality() != null) sb.append(" ").append(cell.getQuality());
                if (cell.getProductionStored() != null && cell.getProductionStored() > 0)
                    sb.append(" 📦x").append(cell.getProductionStored());
                if (Boolean.TRUE.equals(cell.getIsIncubating())) sb.append(" 🥚孵化中");
                sb.append("\n");
            }
        }
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 输入「福地种植」「福地建造」「福地收取」等指令进行操作");
        return sb.toString();
    }

    private String formatSpiritInfo(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();
        sb.append("🧚 【地灵信息】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("形态：").append(status.getSpiritFormName() != null ? status.getSpiritFormName() : "未知形态").append("\n");
        sb.append("MBTI人格：").append(status.getMbtiType().getCode()).append("\n");
        sb.append("语气风格：").append(status.getMbtiType().getToneStyle()).append("\n");
        sb.append("劫数：").append(status.getTribulationStage()).append("\n");
        sb.append("好感度：").append(status.getSpiritAffection()).append("/").append(status.getAffectionMax()).append(" 点\n");
        sb.append("精力值：").append(status.getSpiritEnergy()).append("/").append(status.getEnergyMax()).append("\n");
        sb.append("当前情绪：").append(status.getEmotionState().getEmoji()).append(" ").append(status.getEmotionState().getDescription()).append("\n");
        if (status.getLikedTags() != null && !status.getLikedTags().isEmpty()) {
            sb.append("喜爱：").append(String.join("、", status.getLikedTags())).append("\n");
        }
        if (status.getDislikedTags() != null && !status.getDislikedTags().isEmpty()) {
            sb.append("厌恶：").append(String.join("、", status.getDislikedTags())).append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 与地灵互动可提升好感度 ｜ 输入「地灵送礼 [物品名]」赠送礼物");
        return sb.toString();
    }

    private String formatPlantResult(FarmCellVO result) {
        return "✅ 已在地块 %s 种植%s，预计 %.1f 小时后成熟。".formatted(
                result.getCellId(), result.getCropName(), result.getActualGrowthHours()
        );
    }

    private String formatHatchResult(top.stillmisty.xiantao.domain.land.vo.PenCellVO result) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ 已开始在地块 %s 孵化%s！\n".formatted(result.getCellId(), result.getBeastName()));
        sb.append("品质：%s | 等阶：T%d\n".formatted(result.getQuality(), result.getTier()));
        if (result.isMutant()) {
            sb.append("✨ 变异灵兽！特性：").append(String.join(",", result.getMutationTraits())).append("\n");
        }
        sb.append("预计 %.1f 小时后孵化完成。".formatted(
                java.time.Duration.between(java.time.LocalDateTime.now(), result.getMatureTime()).toHours()
        ));
        return sb.toString();
    }

    private String formatEvolveResult(top.stillmisty.xiantao.domain.land.vo.PenCellVO result, String mode) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ %s成功！\n".formatted(mode));
        sb.append("灵兽：%s | 等阶：T%d | 品质：%s\n".formatted(result.getBeastName(), result.getTier(), result.getQuality()));
        if (result.isMutant()) {
            sb.append("能力：").append(String.join(",", result.getMutationTraits())).append("\n");
        }
        return sb.toString();
    }
}
