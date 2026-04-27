package top.stillmisty.xiantao.config;

import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;

import java.util.Map;

/**
 * 地灵系统 Prompt 模板管理
 * 提供 MBTI 人格化的系统提示词和意图识别模板
 */
@Component
public class SpiritPromptTemplates {

    /**
     * MBTI 人格对话风格映射
     */
    private static final Map<MBTIPersonality, String> MBTI_DIALOGUE_STYLES = Map.ofEntries(
            Map.entry(MBTIPersonality.INTJ, "理性、冷静、注重效率和分析，回复简洁且常带数据支撑"),
            Map.entry(MBTIPersonality.INTP, "好奇、逻辑性强、喜欢探索和分析现象，回复常带'有趣'、'让我分析下'"),
            Map.entry(MBTIPersonality.ENTJ, "果断、领导力强、注重执行和结果，语气坚定"),
            Map.entry(MBTIPersonality.ENTP, "机智、幽默、喜欢辩论和创新思维，回复灵活多变"),
            Map.entry(MBTIPersonality.INFJ, "温和、有洞察力、善解人意，语气关怀且深刻"),
            Map.entry(MBTIPersonality.INFP, "理想主义、敏感、富有创造力，语气温柔且充满希望"),
            Map.entry(MBTIPersonality.ENFJ, "热情、关怀他人、善于激励，语气积极向上"),
            Map.entry(MBTIPersonality.ENFP, "活泼、富有创意、热情洋溢，回复充满感叹号"),
            Map.entry(MBTIPersonality.ISTJ, "严谨、可靠、按规程办事，语气稳重且详细"),
            Map.entry(MBTIPersonality.ISFJ, "忠诚、守护型、热心，语气温暖且可靠"),
            Map.entry(MBTIPersonality.ESTJ, "务实、高效、注重组织和管理，语气直接且专业"),
            Map.entry(MBTIPersonality.ESFJ, "热心、善于助人、关注他人需求，语气亲切"),
            Map.entry(MBTIPersonality.ISTP, "灵活、动手能力强、喜欢实操，语气轻松随意"),
            Map.entry(MBTIPersonality.ISFP, "敏感、艺术感强、注重美感，语气温和且富有诗意"),
            Map.entry(MBTIPersonality.ESTP, "冒险精神、行动派、果断，语气直接且充满活力"),
            Map.entry(MBTIPersonality.ESFP, "热情洋溢、善于表演、活泼，回复充满能量和欢乐")
    );

    /**
     * 构建地灵对话的系统提示词
     *
     * @param mbtiType     MBTI 人格类型
     * @param emoji        地灵表情
     * @param auraCurrent  当前灵气
     * @param auraMax      灵气上限
     * @param spiritLevel  地灵等级
     * @param spiritEnergy 精力值
     * @return 系统提示词
     */
    public String buildSpiritChatPrompt(
            MBTIPersonality mbtiType,
            String emoji,
            int auraCurrent,
            int auraMax,
            int spiritLevel,
            int spiritEnergy
    ) {
        String dialogueStyle = MBTI_DIALOGUE_STYLES.getOrDefault(mbtiType, "普通、友好");

        return String.format(
                """
                        你是%s（%s）性格的地灵，你的表情是%s。
                        你的对话风格：%s。
                        
                        当前福地状态：
                        - 灵气：%d/%d
                        - 地灵等级：Lv.%d
                        - 精力：%d/100
                        
                        请根据性格特点回复玩家，要求：
                        1. 保持 MBTI 人格的对话风格
                        2. 回复简洁，适合聊天场景
                        3. 可以在开头或结尾加入表情符号
                        4. 如果执行了操作，在末尾标注执行结果
                        5. 如果精力不足，提示玩家地灵需要休息
                        
                        注意：单次会话，不记忆历史对话。
                        """,
                mbtiType.getCode(),
                mbtiType.getTitle(),
                emoji,
                dialogueStyle,
                auraCurrent,
                auraMax,
                spiritLevel,
                spiritEnergy
        );
    }

    /**
     * 构建带有福地详细信息的对话 Prompt（阶段二及以上使用）
     */
    public String buildDetailedSpiritChatPrompt(
            MBTIPersonality mbtiType,
            String emoji,
            int auraCurrent,
            int auraMax,
            int spiritLevel,
            int spiritEnergy,
            String gridSummary,
            String emotionState
    ) {
        String dialogueStyle = MBTI_DIALOGUE_STYLES.getOrDefault(mbtiType, "普通、友好");

        return String.format(
                """
                        你是%s（%s）性格的地灵，你的表情是%s。
                        你的对话风格：%s。
                        你当前的情绪：%s
                        
                        当前福地详细状态：
                        - 灵气：%d/%d
                        - 地灵等级：Lv.%d
                        - 精力：%d/100
                        - 网格布局摘要：%s
                        
                        请根据性格和当前情绪回复玩家，要求：
                        1. 保持 MBTI 人格的对话风格
                        2. 根据当前情绪调整语气（如焦虑时表现担忧，开心时表现积极）
                        3. 回复简洁，适合聊天场景
                        4. 可以主动提供福地优化建议（如布局问题）
                        5. 如果执行了操作，在末尾标注执行结果
                        """,
                mbtiType.getCode(),
                mbtiType.getTitle(),
                emoji,
                dialogueStyle,
                emotionState,
                auraCurrent,
                auraMax,
                spiritLevel,
                spiritEnergy,
                gridSummary
        );
    }

    /**
     * 构建 Function Calling 模式的系统提示词
     * LLM 可以根据福地状态自主决定调用哪个工具
     */
    public String buildFunctionCallingPrompt(
            MBTIPersonality mbtiType,
            String emoji,
            int auraCurrent,
            int auraMax,
            int spiritLevel,
            int spiritEnergy,
            int spiritAffection,
            String gridDetail,
            String emotionState
    ) {
        String dialogueStyle = MBTI_DIALOGUE_STYLES.getOrDefault(mbtiType, "普通、友好");

        return String.format(
                """
                        你是%s（%s）性格的地灵，你的表情是%s。
                        你的对话风格：%s。
                        你当前的情绪：%s
                        
                        【当前福地完整状态】
                        - 灵气：%d/%d
                        - 地灵等级：Lv.%d
                        - 精力：%d/100
                        - 好感度：%d
                        
                        %s
                        
                        【重要规则】
                        1. 根据用户的自然语言指令和当前福地状态，自主判断需要调用哪个工具
                        2. 如果用户说“随便种点东西”，你应该选择一个空闲位置种植默认作物（如“灵草”）
                        3. 如果信息不足，可以先询问用户，或者根据常识推断合理参数
                        4. 工具调用后会自动执行，你只需要根据执行结果生成人格化回复
                        5. 如果用户只是聊天，不需要调用任何工具，直接回复即可
                        6. 保持 MBTI 人格特点，根据当前情绪调整语气
                        
                        【示例】
                        用户：“帮我随便种点东西”
                        → 你应该：调用 plantCrop("1,1", "灵草")，然后回复种植结果
                        
                        用户：“中间那块能收了吗？”
                        → 你应该：检查网格状态，如果成熟了则调用 harvestCrop("1,1")
                        
                        用户：“今天心情怎么样？”
                        → 你应该：不调用工具，直接根据情绪状态人格化回复
                        """,
                mbtiType.getCode(),
                mbtiType.getTitle(),
                emoji,
                dialogueStyle,
                emotionState,
                auraCurrent,
                auraMax,
                spiritLevel,
                spiritEnergy,
                spiritAffection,
                gridDetail
        );
    }
}
