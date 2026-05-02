package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.service.UserContext;

/**
 * 地灵情绪工具
 * 供 LLM 通过 Function Calling 自主判断和更新情绪状态
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpiritEmotionTools {

    private final SpiritRepository spiritRepository;
    private final FudiRepository fudiRepository;

    /**
     * 更新地灵情绪状态
     */
    @Tool(description = "更新地灵的情绪状态。可选状态：CALM(平静), HAPPY(开心), ANGRY(生气), EXCITED(兴奋), FATIGUED(疲惫), CURIOUS(好奇), BORED(无聊), SAD(伤心)")
    public String updateEmotion(
            @ToolParam(description = "新的情绪状态") String emotionState
    ) {
        Long userId = UserContext.CURRENT_USER.get();
        if (userId == null) {
            return "无法获取用户信息";
        }

        try {
            EmotionState newState = EmotionState.valueOf(emotionState.toUpperCase());

            Spirit spirit = spiritRepository.findByFudiId(getFudiId(userId))
                    .orElseThrow(() -> new IllegalStateException("地灵不存在"));

            spirit.setEmotionState(newState);
            spiritRepository.save(spirit);

            log.info("地灵情绪更新 - userId: {}, emotion: {}", userId, newState);
            return "情绪已更新为：" + newState.getDescription();
        } catch (IllegalArgumentException e) {
            return "无效的情绪状态：" + emotionState + "。可选：CALM, HAPPY, ANGRY, EXCITED, FATIGUED, CURIOUS, BORED, SAD";
        } catch (Exception e) {
            log.error("更新情绪失败 - userId: {}, error: {}", userId, e.getMessage());
            return "更新情绪失败：" + e.getMessage();
        }
    }

    /**
     * 添加地灵想法/记忆
     */
    @Tool(description = "记录地灵的想法或重要事件到记忆中")
    public String addThought(
            @ToolParam(description = "要记录的想法或事件内容") String thought
    ) {
        Long userId = UserContext.CURRENT_USER.get();
        if (userId == null) {
            return "无法获取用户信息";
        }

        try {
            Spirit spirit = spiritRepository.findByFudiId(getFudiId(userId))
                    .orElseThrow(() -> new IllegalStateException("地灵不存在"));

            // 暂时只记录日志，不存储到数据库
            log.info("地灵想法记录 - userId: {}, thought: {}", userId, thought);
            return "想法已记录";
        } catch (Exception e) {
            log.error("记录想法失败 - userId: {}, error: {}", userId, e.getMessage());
            return "记录想法失败：" + e.getMessage();
        }
    }

    private Long getFudiId(Long userId) {
        return fudiRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("未找到福地"))
                .getId();
    }
}
