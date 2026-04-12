package top.stillmisty.xiantao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.config.SpiritPromptTemplates;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.enums.CellType;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;
import top.stillmisty.xiantao.domain.land.enums.SpiritStage;
import top.stillmisty.xiantao.domain.land.enums.WuxingType;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.domain.land.vo.SpiritIntentVO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 地灵对话核心服务
 * 提供 MBTI 人格化对话和意图识别功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritChatService {

    private final ChatClient spiritChatClient;
    private final FudiRepository fudiRepository;
    private final SpiritPromptTemplates promptTemplates;
    private final FudiService fudiService;
    private final SpiritTools spiritTools;  // 注入工具类，用于 Function Calling
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${xiantao.spirit.enable-fallback:true}")
    private boolean enableFallback;

    @Value("${xiantao.spirit.max-tokens:800}")
    private int maxTokens;

    /**
     * 与地灵进行 MBTI 人格化对话
     * 单次会话，无记忆
     *
     * @param userId 用户 ID
     * @param userInput 用户输入
     * @return 地灵的人格化回复
     */
    public String chatWithSpirit(Long userId, String userInput) {
        try {
            Fudi fudi = fudiRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("未找到福地"));

            // 更新灵气（懒加载）
            fudi.updateAura();
            fudi.updateEmotionState();

            // 构建系统提示词
            String systemPrompt = buildSystemPrompt(fudi);

            // 调用 LLM
            String response = spiritChatClient.prompt()
                    .system(systemPrompt)
                    .user(userInput)
                    .call()
                    .content();

            log.info("地灵对话成功 - userId: {}, mbti: {}, input: {}", userId, fudi.getMbtiType(), userInput);
            return response;

        } catch (Exception e) {
            log.error("地灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
            
            if (enableFallback) {
                log.info("启用降级方案，使用规则匹配回复");
                return fallbackChatResponse(userId, userInput);
            }
            
            return "地灵暂时无法回应，请稍后再试。";
        }
    }

    /**
     * 解析用户意图（自然语言转结构化操作）
     *
     * @param userId 用户 ID
     * @param userInput 用户输入
     * @return 意图识别结果
     */
    public SpiritIntentVO parseIntent(Long userId, String userInput) {
        try {
            Fudi fudi = fudiRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("未找到福地"));

            // 构建意图识别 Prompt
            String systemPrompt = promptTemplates.buildIntentRecognitionPrompt(fudi.getMbtiType());

            // 调用 LLM 进行意图识别
            String jsonResponse = spiritChatClient.prompt()
                    .system(systemPrompt)
                    .user(userInput)
                    .call()
                    .content();

            // 解析 JSON 响应
            SpiritIntentVO intent = parseIntentFromJson(jsonResponse);
            intent.setOriginalInput(userInput);

            log.info("意图识别成功 - userId: {}, intent: {}, confidence: {}", 
                    userId, intent.getIntentType(), intent.getConfidence());

            return intent;

        } catch (Exception e) {
            log.error("意图识别失败 - userId: {}, error: {}", userId, e.getMessage(), e);
            
            if (enableFallback) {
                log.info("启用降级方案，使用规则匹配识别意图");
                return fallbackParseIntent(userInput);
            }
            
            return SpiritIntentVO.builder()
                    .intentType(SpiritIntentVO.IntentType.UNKNOWN)
                    .confidence(0.0)
                    .originalInput(userInput)
                    .build();
        }
    }

    /**
     * 执行意图对应的操作
     *
     * @param userId 用户 ID
     * @param intent 意图识别结果
     * @return 操作结果消息
     */
    public String executeIntent(Long userId, SpiritIntentVO intent) {
        if (intent.getIntentType() == null || intent.getIntentType() == SpiritIntentVO.IntentType.UNKNOWN) {
            return "抱歉，我没有理解你的意思，请换个说法。";
        }

        if (intent.getIntentType() == SpiritIntentVO.IntentType.CHAT) {
            return chatWithSpirit(userId, intent.getOriginalInput());
        }

        try {
            Map<String, String> params = intent.getParameters();

            return switch (intent.getIntentType()) {
                case PLANT -> {
                    String position = params.get("position");
                    String cropName = params.get("cropName");
                    yield executePlant(userId, position, cropName);
                }
                case HARVEST -> {
                    String position = params.get("position");
                    yield executeHarvest(userId, position);
                }
                case BUILD -> {
                    String position = params.get("position");
                    String cellType = params.get("cellType");
                    yield executeBuild(userId, position, cellType);
                }
                case REMOVE -> {
                    String position = params.get("position");
                    yield executeRemove(userId, position);
                }
                case SACRIFICE -> {
                    String itemName = params.get("itemName");
                    yield executeSacrifice(userId, itemName);
                }
                case FEED -> {
                    String position = params.get("position");
                    String feedName = params.get("feedName");
                    yield executeFeed(userId, position, feedName);
                }
                case STATUS -> fudiService.getFudiStatus(userId).toString();
                case GRID -> formatGridStatus(userId);
                case AURA -> formatAuraStatus(userId);
                case SPIRIT_INFO -> formatSpiritInfo(userId);
                default -> "该操作暂未实现。";
            };
        } catch (Exception e) {
            log.error("执行意图失败 - userId: {}, intent: {}, error: {}", userId, intent.getIntentType(), e.getMessage(), e);
            return "操作执行失败：" + e.getMessage();
        }
    }

    /**
     * 处理 @地灵 自然语言交互（完整流程：Function Calling -> 人格化回复）
     * 使用 Spring AI 的 Function Calling 机制，让 LLM 自主决定调用哪个工具
     *
     * @param userId 用户 ID
     * @param userInput 用户输入
     * @return 最终回复
     */
    public String processSpiritInteraction(Long userId, String userInput) {
        // 设置用户上下文，供 SpiritTools 使用
        UserContext.setCurrentUserId(userId);
        try {
            Fudi fudi = fudiRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("未找到福地"));

            // 更新灵气和情绪状态（懒加载）
            fudi.updateAura();
            fudi.updateEmotionState();

            // 构建系统提示词，包含福地详细状态
            String systemPrompt = buildSystemPromptWithFullState(fudi);

            // 使用 Function Calling，LLM 会根据用户输入和福地状态自主决定调用哪个工具
            String response = spiritChatClient.prompt()
                    .system(systemPrompt)
                    .user(userInput)
                    .tools(spiritTools)  // 注册所有可用工具
                    .call()
                    .content();

            log.info("地灵交互完成 - userId: {}, input: {}", userId, userInput);
            return response;

        } catch (Exception e) {
            log.error("地灵交互失败 - userId: {}, error: {}", userId, e.getMessage(), e);
            
            if (enableFallback) {
                return fallbackChatResponse(userId, userInput);
            }
            
            return "地灵暂时无法回应，请稍后再试。";
        } finally {
            // 清除用户上下文，防止内存泄漏
            UserContext.clear();
        }
    }

    // ===================== 辅助方法 =====================

    /**
     * 构建包含完整福地状态的系统提示词（用于 Function Calling）
     */
    private String buildSystemPromptWithFullState(Fudi fudi) {
        String emoji = determineEmoji(fudi);
        String gridDetail = buildGridDetailForLLM(fudi);
        String emotionState = fudi.getEmotionState().getDescription();
        
        return promptTemplates.buildFunctionCallingPrompt(
                fudi.getMbtiType(),
                emoji,
                fudi.getAuraCurrent(),
                fudi.getAuraMax(),
                fudi.getSpiritLevel(),
                fudi.getSpiritEnergy(),
                fudi.getSpiritAffection(),
                gridDetail,
                emotionState
        );
    }

    /**
     * 构建系统提示词（根据地灵形态阶段）- 保留用于纯对话场景
     */
    private String buildSystemPrompt(Fudi fudi) {
        String emoji = determineEmoji(fudi);
        
        // 阶段二及以上使用详细信息
        if (fudi.getSpiritStage() != SpiritStage.STAGE_1) {
            String gridSummary = buildGridDetailForLLM(fudi);  // 使用新方法
            String emotionState = fudi.getEmotionState().getDescription();
            
            return promptTemplates.buildDetailedSpiritChatPrompt(
                    fudi.getMbtiType(),
                    emoji,
                    fudi.getAuraCurrent(),
                    fudi.getAuraMax(),
                    fudi.getSpiritLevel(),
                    fudi.getSpiritEnergy(),
                    gridSummary,
                    emotionState
            );
        }

        return promptTemplates.buildSpiritChatPrompt(
                fudi.getMbtiType(),
                emoji,
                fudi.getAuraCurrent(),
                fudi.getAuraMax(),
                fudi.getSpiritLevel(),
                fudi.getSpiritEnergy()
        );
    }

    /**
     * 根据地灵 MBTI 和五行确定 Emoji
     */
    private String determineEmoji(Fudi fudi) {
        return switch (fudi.getMbtiType().getCategory()) {
            case "理性" -> "⚙️";
            case "理想" -> "🌸";
            case "行动" -> "🔥";
            case "关怀" -> "🌊";
            default -> "🐾";
        };
    }

    /**
     * 为 LLM 构建详细的网格状态描述（包含已占地块和可用空位）
     */
    private String buildGridDetailForLLM(Fudi fudi) {
        int gridSize = fudi.getGridSize();
        
        if (fudi.getGridLayout() == null) {
            // 生成所有可用坐标
            StringBuilder sb = new StringBuilder();
            sb.append("福地网格状态（").append(gridSize).append("x").append(gridSize).append("）：\n");
            sb.append("当前为空，所有坐标均可使用。\n");
            sb.append("可用坐标：");
            List<String> emptyPositions = new ArrayList<>();
            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < gridSize; y++) {
                    emptyPositions.add(x + "," + y);
                }
            }
            sb.append(String.join(", ", emptyPositions));
            return sb.toString();
        }

        @SuppressWarnings("unchecked")
        var cells = (java.util.List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        if (cells == null || cells.isEmpty()) {
            // 生成所有可用坐标
            StringBuilder sb = new StringBuilder();
            sb.append("福地网格状态（").append(gridSize).append("x").append(gridSize).append("）：\n");
            sb.append("当前为空，所有坐标均可使用。\n");
            sb.append("可用坐标：");
            List<String> emptyPositions = new ArrayList<>();
            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < gridSize; y++) {
                    emptyPositions.add(x + "," + y);
                }
            }
            sb.append(String.join(", ", emptyPositions));
            return sb.toString();
        }

        // 收集已占地块信息
        Set<String> occupiedPositions = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append("福地网格状态（").append(gridSize).append("x").append(gridSize).append("）：\n");
        sb.append("【已占地块】\n");
        
        for (var cell : cells) {
            String pos = (String) cell.get("pos");
            occupiedPositions.add(pos);
            String type = (String) cell.get("type");
            String name = (String) cell.get("name");
            
            sb.append("- (").append(pos).append(") ").append(type);
            
            if (name != null && !name.isEmpty()) {
                sb.append(" [").append(name).append("]");
            }
            
            // 添加作物生长信息
            if (cell.containsKey("cropName")) {
                String cropName = (String) cell.get("cropName");
                Double progress = (Double) cell.get("growthProgress");
                Boolean isMature = (Boolean) cell.get("isMature");
                
                sb.append(" 种植:").append(cropName);
                if (isMature != null && isMature) {
                    sb.append(" ✅可收获");
                } else if (progress != null) {
                    sb.append(String.format(" (%.0f%%)", progress * 100));
                }
            }
            
            // 添加灵兽信息
            if (cell.containsKey("beastName")) {
                String beastName = (String) cell.get("beastName");
                Integer hunger = (Integer) cell.get("hunger");
                sb.append(" 饲养:").append(beastName);
                if (hunger != null) {
                    sb.append(" 饥饿值:").append(hunger);
                }
            }
            
            sb.append("\n");
        }
        
        // 计算并显示可用空位
        List<String> emptyPositions = new ArrayList<>();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                String pos = x + "," + y;
                if (!occupiedPositions.contains(pos)) {
                    emptyPositions.add(pos);
                }
            }
        }
        
        sb.append("【可用空位】\n");
        if (emptyPositions.isEmpty()) {
            sb.append("福地已满，无可用坐标。");
        } else {
            sb.append("可用坐标：").append(String.join(", ", emptyPositions));
        }
        
        return sb.toString();
    }

    /**
     * 解析 LLM 返回的意图 JSON
     */
    private SpiritIntentVO parseIntentFromJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            
            String intentTypeStr = root.path("intentType").asText();
            double confidence = root.path("confidence").asDouble(0.5);
            
            Map<String, String> parameters = Map.of();
            JsonNode paramsNode = root.path("parameters");
            if (paramsNode.isObject()) {
                var paramsMap = objectMapper.convertValue(paramsNode, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, String> typedMap = (Map<String, String>) paramsMap;
                parameters = typedMap;
            }

            SpiritIntentVO.IntentType intentType;
            try {
                intentType = SpiritIntentVO.IntentType.valueOf(intentTypeStr);
            } catch (IllegalArgumentException e) {
                intentType = SpiritIntentVO.IntentType.UNKNOWN;
            }

            return SpiritIntentVO.builder()
                    .intentType(intentType)
                    .parameters(parameters)
                    .confidence(confidence)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("解析意图 JSON 失败: {}", json, e);
            return SpiritIntentVO.builder()
                    .intentType(SpiritIntentVO.IntentType.UNKNOWN)
                    .confidence(0.0)
                    .build();
        }
    }

    // ===================== 降级方案：规则匹配 =====================

    /**
     * 降级方案：规则匹配对话回复
     */
    private String fallbackChatResponse(Long userId, String userInput) {
        Fudi fudi = fudiRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"));

        MBTIPersonality mbti = fudi.getMbtiType();
        
        // 简单关键词匹配
        if (userInput.contains("状态") || userInput.contains("福地")) {
            return formatFallbackStatusResponse(mbti, fudi);
        }
        
        if (userInput.contains("收获") || userInput.contains("收")) {
            return formatFallbackHarvestResponse(mbti);
        }
        
        if (userInput.contains("种植") || userInput.contains("种")) {
            return formatFallbackPlantResponse(mbti);
        }

        // 默认回复
        return formatFallbackDefaultResponse(mbti, userInput);
    }

    /**
     * 降级方案：规则匹配意图识别
     */
    private SpiritIntentVO fallbackParseIntent(String userInput) {
        String input = userInput.toLowerCase();
        
        // 种植
        if (input.contains("种") || input.contains("种植")) {
            String cropName = extractCropName(input);
            String position = extractPosition(input);
            return SpiritIntentVO.builder()
                    .intentType(SpiritIntentVO.IntentType.PLANT)
                    .parameters(Map.of("position", position, "cropName", cropName))
                    .confidence(0.7)
                    .originalInput(userInput)
                    .build();
        }
        
        // 收获
        if (input.contains("收") || input.contains("收获")) {
            String position = extractPosition(input);
            if (input.contains("全部") || input.contains("所有") || input.contains("all")) {
                position = "all";
            }
            return SpiritIntentVO.builder()
                    .intentType(SpiritIntentVO.IntentType.HARVEST)
                    .parameters(Map.of("position", position))
                    .confidence(0.7)
                    .originalInput(userInput)
                    .build();
        }
        
        // 建造
        if (input.contains("建造") || input.contains("建")) {
            String cellType = extractCellType(input);
            String position = extractPosition(input);
            return SpiritIntentVO.builder()
                    .intentType(SpiritIntentVO.IntentType.BUILD)
                    .parameters(Map.of("position", position, "cellType", cellType))
                    .confidence(0.7)
                    .originalInput(userInput)
                    .build();
        }
        
        // 献祭
        if (input.contains("献祭") || input.contains("转化")) {
            String itemName = extractItemName(input);
            return SpiritIntentVO.builder()
                    .intentType(SpiritIntentVO.IntentType.SACRIFICE)
                    .parameters(Map.of("itemName", itemName))
                    .confidence(0.7)
                    .originalInput(userInput)
                    .build();
        }

        // 默认聊天
        return SpiritIntentVO.builder()
                .intentType(SpiritIntentVO.IntentType.CHAT)
                .confidence(0.5)
                .originalInput(userInput)
                .build();
    }

    /**
     * 从输入中提取作物名称
     */
    private String extractCropName(String input) {
        // 简单实现：提取常见作物名
        String[] crops = {"灵芝", "人参", "火莲", "灵草", "仙草"};
        for (String crop : crops) {
            if (input.contains(crop)) {
                return crop;
            }
        }
        return "灵草"; // 默认
    }

    /**
     * 从输入中提取坐标
     */
    private String extractPosition(String input) {
        // 匹配坐标格式 "x,y"
        Pattern pattern = Pattern.compile("(\\d+,\\d+)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 方位词转换
        if (input.contains("中间") || input.contains("中央")) {
            return "1,1";
        }
        if (input.contains("左上")) {
            return "0,0";
        }
        if (input.contains("右上")) {
            return "2,0";
        }
        if (input.contains("左下")) {
            return "0,2";
        }
        if (input.contains("右下")) {
            return "2,2";
        }
        
        return "1,1"; // 默认中间
    }

    /**
     * 从输入中提取地块类型
     */
    private String extractCellType(String input) {
        if (input.contains("灵田") || input.contains("田")) {
            return "灵田";
        }
        if (input.contains("兽栏") || input.contains("兽")) {
            return "兽栏";
        }
        if (input.contains("阵眼") || input.contains("阵")) {
            return "阵眼";
        }
        return "灵田"; // 默认
    }

    /**
     * 从输入中提取物品名称
     */
    private String extractItemName(String input) {
        if (input.contains("所有") || input.contains("全部") || input.contains("all")) {
            return "all";
        }
        // 简单实现
        return "装备";
    }

    // ===================== 格式化辅助方法 =====================

    private String formatGridStatus(Long userId) {
        Fudi fudi = fudiRepository.findByUserId(userId).orElseThrow();
        StringBuilder sb = new StringBuilder();
        sb.append("🗺️ 【福地网格布局】\n");
        sb.append("网格大小：").append(fudi.getGridSize()).append("x").append(fudi.getGridSize()).append("\n");
        
        if (fudi.getGridLayout() != null && fudi.getGridLayout().containsKey("cells")) {
            @SuppressWarnings("unchecked")
            var cells = (java.util.List<Map<String, Object>>) fudi.getGridLayout().get("cells");
            for (var cell : cells) {
                sb.append("📍 (").append(cell.get("pos")).append(") ");
                sb.append(cell.get("type")).append("\n");
            }
        }
        
        return sb.toString();
    }

    private String formatAuraStatus(Long userId) {
        Fudi fudi = fudiRepository.findByUserId(userId).orElseThrow();
        return String.format("🔮 【灵气详情】\n当前灵气：%d/%d\n每小时消耗：%d",
                fudi.getAuraCurrent(), fudi.getAuraMax(), fudi.calculateHourlyAuraCost());
    }

    private String formatSpiritInfo(Long userId) {
        Fudi fudi = fudiRepository.findByUserId(userId).orElseThrow();
        return String.format("🧚 【地灵信息】\nMBTI人格：%s（%s）\n形态阶段：%s\n等级：Lv.%d",
                fudi.getMbtiType().getCode(), fudi.getMbtiType().getTitle(),
                fudi.getSpiritStage().getName(), fudi.getSpiritLevel());
    }

    // ===================== 执行操作的包装方法 =====================

    private String executePlant(Long userId, String position, String cropName) {
        // TODO: 根据作物名称查询真实的 cropId 和 element
        WuxingType element = WuxingType.WOOD;
        Integer cropId = 101;
        
        fudiService.plantCrop(userId, position, cropId, cropName, element);
        return String.format("已在 (%s) 种植%s。", position, cropName);
    }

    private String executeHarvest(Long userId, String position) {
        if ("all".equalsIgnoreCase(position)) {
            var result = fudiService.harvestAllCrops(userId);
            int harvested = (Integer) result.get("harvested");
            int totalYield = (Integer) result.get("totalYield");
            return harvested == 0 ? "没有可收获的灵田。" : 
                    String.format("已收获 %d 块灵田，共获得 %d 份素材。", harvested, totalYield);
        } else {
            var result = fudiService.harvestCrop(userId, position);
            String cropName = (String) result.get("cropName");
            int yield = (Integer) result.get("yield");
            return String.format("已收获 (%s) 的%s，获得 %d 份。", position, cropName, yield);
        }
    }

    private String executeBuild(Long userId, String position, String cellType) {
        CellType type = CellType.fromChineseName(cellType);
        fudiService.buildCell(userId, position, type);
        return String.format("已在 (%s) 建造%s。", position, cellType);
    }

    private String executeRemove(Long userId, String position) {
        fudiService.removeCell(userId, position);
        return String.format("已拆除 (%s) 的地块。", position);
    }

    private String executeSacrifice(Long userId, String itemName) {
        if ("all".equalsIgnoreCase(itemName)) {
            return "批量献祭功能尚未实现。";
        }
        // TODO: 根据物品名称查找真实 ID
        Long itemId = 1L;
        int auraGain = fudiService.sacrificeItem(userId, itemId);
        return String.format("献祭成功！获得 %d 灵气。", auraGain);
    }

    private String executeFeed(Long userId, String position, String feedName) {
        // TODO: 根据饲料名称查找真实 ID
        Integer feedItemId = 1;
        var result = fudiService.feedBeast(userId, position, feedItemId, feedName);
        String beastName = (String) result.get("beastName");
        int newHunger = (Integer) result.get("newHunger");
        return String.format("已喂养 (%s) 的%s，当前饥饿值：%d", position, beastName, newHunger);
    }

    // ===================== 降级回复格式化 =====================

    private String formatFallbackStatusResponse(MBTIPersonality mbti, Fudi fudi) {
        return switch (mbti) {
            case INTJ, ENTJ -> String.format(
                    "📊 福地状态。灵气：%d/%d，每小时消耗：%d，效率在预期范围内。",
                    fudi.getAuraCurrent(), fudi.getAuraMax(), fudi.calculateHourlyAuraCost());
            case INFP, INFJ -> String.format(
                    "🌿 福地正在运转呢～灵气 %d/%d，我能感受到灵气的流动。每小时消耗 %d 灵气。",
                    fudi.getAuraCurrent(), fudi.getAuraMax(), fudi.calculateHourlyAuraCost());
            case ESTP, ESFP -> String.format(
                    "🔥 福地状态超棒！灵气 %d/%d，消耗 %d/小时，一切正常！",
                    fudi.getAuraCurrent(), fudi.getAuraMax(), fudi.calculateHourlyAuraCost());
            default -> String.format(
                    "福地状态：灵气 %d/%d，每小时消耗 %d 灵气。",
                    fudi.getAuraCurrent(), fudi.getAuraMax(), fudi.calculateHourlyAuraCost());
        };
    }

    private String formatFallbackHarvestResponse(MBTIPersonality mbti) {
        return switch (mbti) {
            case INTJ, ENTJ -> "根据分析，现在可以收获灵田了。请提供具体坐标。";
            case INFP, INFJ -> "灵田应该成熟了吧～你想收获哪一块呢？";
            case ESTP, ESFP -> "好嘞！要收获哪个位置？告诉我就行！";
            default -> "请提供要收获的坐标，或说'全部'收获所有成熟灵田。";
        };
    }

    private String formatFallbackPlantResponse(MBTIPersonality mbti) {
        return switch (mbti) {
            case INTJ, ENTJ -> "种植计划已就绪。请指定坐标和作物类型。";
            case INFP, INFJ -> "想要种些什么呢？告诉我位置和作物名称吧～";
            case ESTP, ESFP -> "来种点什么吧！位置和作物告诉我！";
            default -> "请提供种植位置和作物名称。";
        };
    }

    private String formatFallbackDefaultResponse(MBTIPersonality mbti, String userInput) {
        return switch (mbti) {
            case INTJ, ENTJ -> "我已记录你的需求。请提供更具体的操作指令，以便我执行。";
            case INFP, INFJ -> "嗯...我不太确定你的意思，能再说清楚一点吗？";
            case ENTP, ENFP -> "哈哈，你是想说什么呢？我有点好奇！";
            case ISTJ, ISFJ -> "请按照格式输入指令，我会帮你处理。";
            case ESTP, ESFP -> "没太听懂！你要我做啥？";
            default -> "请提供更详细的指令，例如'种植 0,0 灵芝'或'收获 1,1'。";
        };
    }
}
