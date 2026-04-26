package top.stillmisty.xiantao.handle.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.vo.FarmCellVO;
import top.stillmisty.xiantao.domain.land.vo.FudiStatusVO;
import top.stillmisty.xiantao.service.FudiService;
import top.stillmisty.xiantao.service.SpiritChatService;
import top.stillmisty.xiantao.service.UserAuthService;
import top.stillmisty.xiantao.service.UserService;

import java.util.Map;

/**
 * 福地命令处理器
 * 处理所有福地相关的跨平台命令
 */
@Component
@Slf4j
public class FudiCommandHandler extends BaseCommandHandler {

    private final FudiService fudiService;
    private final SpiritChatService spiritChatService;

    public FudiCommandHandler(
            UserAuthService userAuthService,
            UserService userService,
            FudiService fudiService,
            SpiritChatService spiritChatService
    ) {
        super(userAuthService, userService);
        this.fudiService = fudiService;
        this.spiritChatService = spiritChatService;
    }

    /**
     * 处理 #福地 命令
     */
    public String handleFudiStatus(PlatformType platform, String openId) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            FudiStatusVO status = fudiService.getFudiStatus(authResult.userId());
            return formatFudiStatus(status);
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    /**
     * 处理 #福地网格 命令
     */
    public String handleFudiGrid(PlatformType platform, String openId) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            FudiStatusVO status = fudiService.getFudiStatus(authResult.userId());
            return formatGridLayout(status);
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    /**
     * 处理 #福地灵气 命令
     */
    public String handleFudiAura(PlatformType platform, String openId) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            FudiStatusVO status = fudiService.getFudiStatus(authResult.userId());
            return formatAuraInfo(status);
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    /**
     * 处理 #福地地灵 命令
     */
    public String handleFudiSpirit(PlatformType platform, String openId) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            FudiStatusVO status = fudiService.getFudiStatus(authResult.userId());
            return formatSpiritInfo(status);
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    /**
     * 处理 #种植 命令
     */
    public String handlePlant(PlatformType platform, String openId, String position, String cropName) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            FarmCellVO result = fudiService.plantCropByName(authResult.userId(), position, cropName);
            return formatPlantResult(result);
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("种植失败", e);
            return "❌ 种植失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #收获 命令
     */
    public String handleHarvest(PlatformType platform, String openId, String position) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            if ("all".equalsIgnoreCase(position)) {
                Map<String, Object> result = fudiService.harvestAllCrops(authResult.userId());
                int harvested = (Integer) result.get("harvested");
                int totalYield = (Integer) result.get("totalYield");
                
                if (harvested == 0) {
                    return "🌾 没有可收获的灵田。";
                }
                
                return String.format("✅ 已收获 %d 块灵田，共获得 %d 份素材。", harvested, totalYield);
            } else {
                Map<String, Object> result = fudiService.harvestCrop(authResult.userId(), position);
                String cropName = (String) result.get("cropName");
                int yield = (Integer) result.get("yield");
                
                return String.format("✅ 已收获 (%s) 的%s，获得 %d 份。", position, cropName, yield);
            }
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("收获失败", e);
            return "❌ 收获失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #建造 命令
     */
    public String handleBuild(PlatformType platform, String openId, String position, String cellTypeName) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            CellType cellType = CellType.fromChineseName(cellTypeName);
            Map<String, Object> result = fudiService.buildCell(authResult.userId(), position, cellType);
            
            return String.format("✅ 已在 (%s) 建造%s。", position, cellTypeName);
        } catch (IllegalArgumentException e) {
            return "❌ 不支持的地块类型：" + cellTypeName + "（可选：灵田、兽栏、阵眼）";
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("建造失败", e);
            return "❌ 建造失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #拆除 命令
     */
    public String handleRemove(PlatformType platform, String openId, String position) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            Map<String, Object> result = fudiService.removeCell(authResult.userId(), position);
            String typeName = (String) result.get("type");
            
            return String.format("✅ 已拆除 (%s) 的%s。", position, typeName);
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("拆除失败", e);
            return "❌ 拆除失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #献祭 命令
     */
    public String handleSacrifice(PlatformType platform, String openId, String itemName) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            if ("all".equalsIgnoreCase(itemName)) {
                // TODO: 实现批量献祭
                return "⚠️ 批量献祭功能尚未实现。";
            }
            
            int auraGain = fudiService.sacrificeItemByName(authResult.userId(), itemName);
            return String.format("✅ 献祭成功！%s 转化为 %d 灵气。", itemName, auraGain);
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("献祭失败", e);
            return "❌ 献祭失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #喂养 命令
     */
    public String handleFeed(PlatformType platform, String openId, String position, String feedItemName) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            Map<String, Object> result = fudiService.feedBeastByName(authResult.userId(), position, feedItemName);
            return formatFeedResult(result);
        } catch (IllegalStateException e) {
            return "❌ " + e.getMessage();
        } catch (Exception e) {
            log.error("喂养失败", e);
            return "❌ 喂养失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #福地自动 命令
     */
    public String handleAutoMode(PlatformType platform, String openId, String mode) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            // TODO: 实现自动模式切换
            boolean enable = "开".equals(mode) || "on".equalsIgnoreCase(mode);
            return String.format("✅ 福地自动管理模式已%s。", enable ? "开启" : "关闭");
        } catch (Exception e) {
            log.error("切换自动模式失败", e);
            return "❌ 操作失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #福地升级 命令
     */
    public String handleUpgrade(PlatformType platform, String openId) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            // TODO: 实现聚灵核心升级逻辑
            return "⚠️ 福地升级功能尚未实现。";
        } catch (Exception e) {
            log.error("福地升级失败", e);
            return "❌ 升级失败：" + e.getMessage();
        }
    }

    /**
     * 处理 #福地扩建 命令
     */
    public String handleExpand(PlatformType platform, String openId) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }
        
        try {
            // TODO: 实现福地扩建逻辑
            return "⚠️ 福地扩建功能尚未实现。";
        } catch (Exception e) {
            log.error("福地扩建失败", e);
            return "❌ 扩建失败：" + e.getMessage();
        }
    }

    // ===================== 格式化输出方法 =====================

    /**
     * 格式化福地整体状态
     */
    private String formatFudiStatus(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏔️ 【福地状态】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("🔮 灵气：").append(status.getAuraCurrent()).append("/").append(status.getAuraMax()).append("\n");
        sb.append("⚡ 每小时消耗：").append(status.getAuraHourlyCost()).append(" 灵气\n");
        sb.append("🌟 地灵等级：Lv.").append(status.getSpiritLevel()).append("\n");
        sb.append("🎭 地灵人格：").append(status.getMbtiType().getCode()).append("（").append(status.getMbtiType().getTitle()).append("）\n");
        sb.append("📊 形态阶段：").append(status.getSpiritStage().getName()).append("\n");
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

    /**
     * 格式化网格布局
     */
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
                
                if (cell.getName() != null) {
                    sb.append(" - ").append(cell.getName());
                }
                
                if (cell.getGrowthProgress() != null) {
                    int percent = (int) (cell.getGrowthProgress() * 100);
                    sb.append(" [").append(percent).append("%]");
                    if (Boolean.TRUE.equals(cell.getIsMature())) {
                        sb.append(" ✅ 可收获");
                    }
                }
                
                if (cell.getHunger() != null) {
                    sb.append(" 饥饿值:").append(cell.getHunger());
                }
                
                if (cell.getDurability() != null) {
                    sb.append(" 耐久:").append(cell.getDurability());
                }
                
                sb.append("\n");
            }
        }
        
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 使用 #种植/#建造/#收获 进行操作");
        
        return sb.toString();
    }

    /**
     * 格式化灵气信息
     */
    private String formatAuraInfo(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();
        int current = status.getAuraCurrent();
        int max = status.getAuraMax();
        int hourlyCost = status.getAuraHourlyCost();
        int percent = (int) ((double) current / max * 100);
        
        sb.append("🔮 【灵气详情】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("当前灵气：").append(current).append("/").append(max).append(" (").append(percent).append("%)\n");
        sb.append("每小时消耗：").append(hourlyCost).append(" 灵气\n");
        
        if (hourlyCost > 0) {
            int hoursRemaining = current / hourlyCost;
            sb.append("预计可用：").append(hoursRemaining).append(" 小时\n");
        }
        
        sb.append("聚灵核心等级：Lv.").append(status.getCoreLevel()).append("\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 使用 #献祭 装备可补充灵气");
        
        return sb.toString();
    }

    /**
     * 格式化地灵信息
     */
    private String formatSpiritInfo(FudiStatusVO status) {
        StringBuilder sb = new StringBuilder();
        sb.append("🧚 【地灵信息】\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("MBTI人格：").append(status.getMbtiType().getCode()).append("（").append(status.getMbtiType().getTitle()).append("）\n");
        sb.append("基础表情：").append(status.getMbtiType().getEmoji()).append("\n");
        sb.append("形态阶段：").append(status.getSpiritStage().getName()).append("\n");
        sb.append("  ┗ 特性：").append(status.getSpiritStage().getFeatures()).append("\n");
        sb.append("地灵等级：Lv.").append(status.getSpiritLevel()).append("\n");
        sb.append("好感度：").append(status.getSpiritAffection()).append(" 点\n");
        sb.append("精力值：").append(status.getSpiritEnergy()).append("/100\n");
        sb.append("当前情绪：").append(status.getEmotionState().getEmoji()).append(" ").append(status.getEmotionState().getDescription()).append("\n");
        sb.append("━━━━━━━━━━━━━━━\n");
        sb.append("💡 与地灵互动可提升好感度");

        return sb.toString();
    }

    // ===================== 格式化输出方法 =====================

    /**
     * 格式化种植结果
     */
    private String formatPlantResult(FarmCellVO result) {
        return String.format("✅ 已在 (%s) 种植%s，预计 %.1f 小时后成熟。\n五行属性：%s\n生长修正：%.0f%%",
                result.getPosition(),
                result.getCropName(),
                result.getActualGrowthHours(),
                result.getElement().getChineseName(),
                result.getGrowthModifier() * 100);
    }

    /**
     * 格式化喂养结果
     */
    private String formatFeedResult(Map<String, Object> result) {
        String beastName = (String) result.get("beastName");
        int newHunger = (Integer) result.get("newHunger");
        String position = (String) result.get("position");

        return String.format("✅ 已喂养 (%s) 的%s，当前饥饿值：%d", position, beastName, newHunger);
    }

    /**
     * 格式化福地整体状态
     */
    public String handleSpiritChat(PlatformType platform, String openId, String userInput) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        try {
            log.info("处理地灵自然语言交互 - userId: {}, input: {}", authResult.userId(), userInput);

            return spiritChatService.processSpiritInteraction(authResult.userId(), userInput);
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("地灵自然语言交互失败", e);
            return "❌ 地灵暂时无法回应，请稍后再试。";
        }
    }

    /**
     * 处理纯地灵对话（不执行操作，仅对话）
     *
     * @param platform 平台类型
     * @param openId 用户 OpenID
     * @param userInput 用户输入
     * @return 地灵的人格化回复
     */
    public String handleSpiritPureChat(PlatformType platform, String openId, String userInput) {
        AuthResult authResult = authenticateAndValidateUser(platform, openId);
        if (!authResult.authenticated()) {
            return authResult.errorMessage();
        }

        try {
            log.info("处理地灵纯对话 - userId: {}, input: {}", authResult.userId(), userInput);

            return spiritChatService.chatWithSpirit(authResult.userId(), userInput);
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error("地灵纯对话失败", e);
            return "❌ 地灵暂时无法回应，请稍后再试。";
        }
    }
}
