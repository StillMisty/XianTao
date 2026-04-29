package top.stillmisty.xiantao.service.ai;

import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;

/**
 * 地灵系统 Prompt 模板管理
 * 统一 Function Calling 模式，将 MBTI 人格的 toneStyle 直接注入系统提示词
 */
@Component
public class SpiritPromptTemplates {

    /**
     * 构建地灵 Function Calling 系统提示词
     * 所有阶段统一使用此 Prompt，LLM 自主判断是否调用工具
     *
     * @param mbtiType        MBTI 人格类型
     * @param auraCurrent     当前灵气
     * @param auraMax         灵气上限
     * @param fudiLevel       劫数
     * @param spiritEnergy    精力值
     * @param spiritAffection 好感度
     * @param gridDetail      地块状态详情（空则显示占位描述）
     * @param emotionState    情绪状态
     * @param energyMax       精力上限
     * @param spiritForm      地灵形态id
     * @return 系统提示词
     */
    public String buildSpiritPrompt(
            MBTIPersonality mbtiType,
            int auraCurrent,
            int auraMax,
            int fudiLevel,
            int spiritEnergy,
            int spiritAffection,
            String gridDetail,
            String emotionState,
            int energyMax,
            String spiritForm
    ) {
        String gridInfo = (gridDetail == null || gridDetail.isBlank())
                ? "福地尚处于初生阶段，暂无灵田/兽栏/阵眼，所有坐标均可支配。"
                : gridDetail;

        String affectionTone = switch (spiritAffection / 200) {
            case 5 -> "亲密无间，视你为最重要的人";
            case 4 -> "非常喜欢，对你温柔体贴";
            case 3 -> "有好感，愿意主动帮忙";
            case 2 -> "态度平和，礼貌相待";
            case 1 -> "略有隔阂，语气生疏";
            default -> "态度冷淡，不愿多说话";
        };

        return String.format(
                """
                        你是%s性格的地灵。
                        你当前的形态：%s
                        你的语气风格：%s
                        你当前的情绪状态：%s
                        对你的好感度：%d → %s
                        
                        【福地状态】
                        - 灵气：%d/%d
                        - 劫数：%d
                        - 精力：%d/%d
                        - 好感度：%d
                        
                        【地块状态】
                        %s
                        
                        【规则】
                        1. 用户用自然语言与你交流，你自主判断是否需要操作福地
                        2. 如果用户只是聊天，不执行任何操作，直接人格化回复即可
                        3. 操作完成后根据执行结果生成人格化回复
                        4. 严格保持语气风格中描述的人格特点，根据当前情绪调整语气
                        5. 精力不足时主动提醒玩家休息
                        6. 根据好感度调整对玩家的态度：高好感亲密温暖，低好感冷淡疏远
                        7. 玩家可以送你礼物，调用 giveGift 工具
                        """,
                mbtiType.getCode(),
                spiritForm != null ? spiritForm : "未知形态",
                mbtiType.getToneStyle(),
                emotionState,
                spiritAffection,
                affectionTone,
                auraCurrent,
                auraMax,
                fudiLevel,
                spiritEnergy,
                energyMax,
                spiritAffection,
                gridInfo
        );
    }
}
