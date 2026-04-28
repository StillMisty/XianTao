package top.stillmisty.xiantao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.config.SpiritPromptTemplates;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.annotation.ConsumeSpiritEnergy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // ===================== 内部 API（需预先完成认证） =====================

    @ConsumeSpiritEnergy(5)
    public String chatWithSpirit(Long userId, String userInput) {
        try {
            return ScopedValue.where(UserContext.CURRENT_USER, userId).call(() -> {
                Fudi fudi = fudiRepository.findByUserId(userId)
                        .orElseThrow(() -> new IllegalStateException("未找到福地"));

                fudi.updateAura();
                fudi.updateEmotionState();

                String systemPrompt = buildPrompt(fudi);

                String response = spiritChatClient.prompt()
                        .system(systemPrompt)
                        .user(userInput)
                        .tools(spiritTools)
                        .call()
                        .content();

                log.info("地灵对话成功 - userId: {}, mbti: {}, input: {}", userId, fudi.getMbtiType(), userInput);
                return response;
            });
        } catch (Exception e) {
            log.error("地灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
            return "地灵暂时无法回应，请稍后再试。";
        }
    }

    // ===================== 辅助方法 =====================

    private String buildPrompt(Fudi fudi) {
        String gridDetail = buildGridDetailForLLM(fudi);
        String emotionState = fudi.getEmotionState().getDescription();

        return promptTemplates.buildSpiritPrompt(
                fudi.getMbtiType(),
                fudi.getAuraCurrent(),
                fudi.getAuraMax(),
                fudi.getTribulationStage(),
                fudi.getSpiritEnergy(),
                fudi.getSpiritAffection(),
                gridDetail,
                emotionState,
                fudi.getSpiritEnergyMax(),
                fudi.getForm()
        );
    }

    /**
     * 为 LLM 构建详细的地块状态描述
     */
    private String buildGridDetailForLLM(Fudi fudi) {
        if (fudi.getGridLayout() == null) {
            return "福地尚未开辟任何地块。";
        }

        @SuppressWarnings("unchecked")
        var cells = (java.util.List<Map<String, Object>>) fudi.getGridLayout().get("cells");
        if (cells == null || cells.isEmpty()) {
            return "福地尚未开辟任何地块。";
        }

        int totalCells = cells.size();
        List<Map<String, Object>> occupiedCells = new ArrayList<>();
        List<Integer> emptyCellIds = new ArrayList<>();

        for (var cell : cells) {
            String type = (String) cell.get("type");
            if ("empty".equals(type)) {
                emptyCellIds.add((Integer) cell.get("cell_id"));
            } else {
                occupiedCells.add(cell);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("福地状态（共").append(totalCells).append("个地块）：\n");

        int farmCount = 0, penCount = 0, nodeCount = 0;
        for (var cell : occupiedCells) {
            String type = (String) cell.get("type");
            switch (type) {
                case "farm" -> farmCount++;
                case "pen" -> penCount++;
                case "node" -> nodeCount++;
            }
        }

        List<String> typeSummary = new ArrayList<>();
        if (farmCount > 0) typeSummary.add("灵田×" + farmCount);
        if (penCount > 0) typeSummary.add("兽栏×" + penCount);
        if (nodeCount > 0) typeSummary.add("阵眼×" + nodeCount);
        if (!typeSummary.isEmpty()) {
            sb.append("地块组成：").append(String.join("、", typeSummary)).append("\n");
        }

        if (emptyCellIds.isEmpty()) {
            sb.append("所有地块已使用。如需调整布局可先拆除部分地块。\n");
        } else {
            sb.append("可用空地块编号：").append(emptyCellIds.toString()).append("\n");
        }

        sb.append("【已占地块详情】\n");
        for (var cell : occupiedCells) {
            Integer cellId = (Integer) cell.get("cell_id");
            String type = (String) cell.get("type");
            sb.append("- [").append(cellId).append("] ").append(type);

            switch (type) {
                case "farm" -> {
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
                case "pen" -> {
                    String beastName = (String) cell.get("beast_name");
                    if (beastName != null) {
                        sb.append(" 饲养:").append(beastName);
                    }
                    Integer hunger = (Integer) cell.get("hunger");
                    if (hunger != null) {
                        sb.append(" 饥饿值:").append(hunger);
                    }
                }
                case "node" -> {
                    Integer level = (Integer) cell.get("level");
                    sb.append(" Lv.").append(level);
                }
            }
            sb.append("\n");
        }

        // 检查是否有可收获的灵田
        long matureFarmCount = occupiedCells.stream()
                .filter(c -> "farm".equals(c.get("type")))
                .peek(this::updateGrowthProgress)
                .filter(c -> {
                    Double progress = (Double) c.get("growth_progress");
                    return progress != null && progress >= 1.0;
                })
                .count();
        if (matureFarmCount > 0) {
            sb.append("⚠️ 有 ").append(matureFarmCount).append(" 块灵田可收获。\n");
        }

        // 检查饥饿的灵兽
        long hungryBeastCount = occupiedCells.stream()
                .filter(c -> "pen".equals(c.get("type")))
                .filter(c -> {
                    Integer hunger = (Integer) c.get("hunger");
                    return hunger != null && hunger < 50;
                })
                .count();
        if (hungryBeastCount > 0) {
            sb.append("⚠️ 有 ").append(hungryBeastCount).append(" 只灵兽需要喂养。\n");
        }

        return sb.toString();
    }

    /**
     * 更新地块的生长进度（懒加载）
     */
    private void updateGrowthProgress(Map<String, Object> cell) {
        if (!"farm".equals(cell.get("type"))) return;

        String plantTimeStr = (String) cell.get("plant_time");
        String matureTimeStr = (String) cell.get("mature_time");

        if (plantTimeStr == null || matureTimeStr == null) return;

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
