package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.config.SpiritPromptTemplates;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.enums.SpiritStage;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;

import java.time.LocalDateTime;
import java.util.*;

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
    private final SpiritTools spiritTools;
    private final AuthenticationService authService;

    // ===================== 公开 API（含认证） =====================

    public ServiceResult<String> chatWithSpirit(PlatformType platform, String openId, String userInput) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(chatWithSpirit(auth.userId(), userInput));
    }

    public ServiceResult<String> processSpiritInteraction(PlatformType platform, String openId, String userInput) {
        var auth = authService.authenticateAndValidateUser(platform, openId);
        if (!auth.authenticated()) return new ServiceResult.Failure<>(auth.errorMessage());
        return new ServiceResult.Success<>(processSpiritInteraction(auth.userId(), userInput));
    }

    // ===================== 内部 API（需预先完成认证） =====================

    /**
     * 与地灵进行 MBTI 人格化对话
     * 单次会话，无记忆
     *
     * @param userId    用户 ID
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
            return "地灵暂时无法回应，请稍后再试。";
        }
    }


    /**
     * 处理 @地灵 自然语言交互（完整流程：Function Calling -> 人格化回复）
     * 使用 Spring AI 的 Function Calling 机制，让 LLM 自主决定调用哪个工具
     *
     * @param userId    用户 ID
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

        // 统计各地块类型数量
        Map<String, Integer> typeCount = new HashMap<>();
        List<Map<String, Object>> farmCells = new ArrayList<>();
        List<Map<String, Object>> penCells = new ArrayList<>();
        List<Map<String, Object>> nodeCells = new ArrayList<>();

        for (var cell : cells) {
            String pos = (String) cell.get("pos");
            occupiedPositions.add(pos);
            String type = (String) cell.get("type");
            typeCount.merge(type, 1, Integer::sum);

            switch (type) {
                case "farm" -> farmCells.add(cell);
                case "pen" -> penCells.add(cell);
                case "node" -> nodeCells.add(cell);
                default -> {
                }
            }
        }

        // 计算可用空位
        List<String> emptyPositions = new ArrayList<>();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                String pos = x + "," + y;
                if (!occupiedPositions.contains(pos)) {
                    emptyPositions.add(pos);
                }
            }
        }

        // 如果福地已满，提供简洁摘要
        if (emptyPositions.isEmpty()) {
            sb.append("【布局摘要】福地已满（").append(occupiedPositions.size()).append("/").append(gridSize * gridSize).append("）。\n");

            // 按类型汇总
            if (!typeCount.isEmpty()) {
                List<String> typeSummary = new ArrayList<>();
                if (typeCount.containsKey("farm")) {
                    typeSummary.add("灵田×" + typeCount.get("farm"));
                }
                if (typeCount.containsKey("pen")) {
                    typeSummary.add("兽栏×" + typeCount.get("pen"));
                }
                if (typeCount.containsKey("node")) {
                    typeSummary.add("阵眼×" + typeCount.get("node"));
                }
                sb.append("地块组成：").append(String.join("、", typeSummary)).append("\n");
            }

            // 显示可收获的灵田
            long matureFarmCount = farmCells.stream()
                    .peek(this::updateGrowthProgress)
                    .filter(cell -> {
                        Double progress = (Double) cell.get("growth_progress");
                        return progress != null && progress >= 1.0;
                    })
                    .count();
            if (matureFarmCount > 0) {
                sb.append("⚠️ 有 ").append(matureFarmCount).append(" 块灵田可收获。\n");
            }

            // 显示饥饿的灵兽
            long hungryBeastCount = penCells.stream()
                    .filter(cell -> {
                        Integer hunger = (Integer) cell.get("hunger");
                        return hunger != null && hunger < 50;
                    })
                    .count();
            if (hungryBeastCount > 0) {
                sb.append("⚠️ 有 ").append(hungryBeastCount).append(" 只灵兽需要喂养。\n");
            }

            sb.append("💡 提示：如需调整布局，可先拆除部分地块或等待福地升级扩建。");
        } else {
            // 有空位时，显示详细列表
            sb.append("【已占地块】\n");
            for (var cell : cells) {
                String pos = (String) cell.get("pos");
                String type = (String) cell.get("type");
                String name = (String) cell.get("name");

                sb.append("- (").append(pos).append(") ").append(type);

                if (name != null && !name.isEmpty()) {
                    sb.append(" [").append(name).append("]");
                }

                // 添加作物生长信息
                if ("farm".equals(type)) {
                    // 懒加载计算生长进度
                    updateGrowthProgress(cell);

                    String cropName = (String) cell.get("crop_name");
                    Double progress = (Double) cell.get("growth_progress");
                    Boolean isMature = progress != null && progress >= 1.0;

                    if (cropName != null) {
                        sb.append(" 种植:").append(cropName);
                        if (isMature) {
                            sb.append(" ✅可收获");
                        } else if (progress != null) {
                            sb.append(String.format(" (%.0f%%)", progress * 100));
                        }
                    }
                }

                // 添加灵兽信息
                if ("pen".equals(type)) {
                    String beastName = (String) cell.get("beast_name");
                    Integer hunger = (Integer) cell.get("hunger");
                    if (beastName != null) {
                        sb.append(" 饲养:").append(beastName);
                    }
                    if (hunger != null) {
                        sb.append(" 饥饿值:").append(hunger);
                    }
                }

                sb.append("\n");
            }

            sb.append("【可用空位】\n");
            sb.append("可用坐标：").append(String.join(", ", emptyPositions));
        }

        return sb.toString();
    }

    /**
     * 更新地块的生长进度（懒加载）
     */
    private void updateGrowthProgress(Map<String, Object> cell) {
        if (!"farm".equals(cell.get("type"))) {
            return;
        }

        String plantTimeStr = (String) cell.get("plant_time");
        String matureTimeStr = (String) cell.get("mature_time");

        if (plantTimeStr == null || matureTimeStr == null) {
            return;
        }

        try {
            LocalDateTime plantTime = LocalDateTime.parse(plantTimeStr);
            LocalDateTime matureTime = LocalDateTime.parse(matureTimeStr);
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(matureTime) || now.isEqual(matureTime)) {
                cell.put("growth_progress", 1.0);
            } else {
                long totalSeconds = java.time.Duration.between(plantTime, matureTime).getSeconds();
                long elapsedSeconds = java.time.Duration.between(plantTime, now).getSeconds();
                double progress = Math.min(1.0, (double) elapsedSeconds / totalSeconds);
                cell.put("growth_progress", progress);
            }
        } catch (Exception e) {
            // 解析失败，保持原值
        }
    }


}
