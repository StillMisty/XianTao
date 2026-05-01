package top.stillmisty.xiantao.service.ai;

import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.land.enums.MBTIPersonality;

@Component
public class SpiritPromptTemplates {

    public String buildSpiritPrompt(
            MBTIPersonality mbtiType,
            int fudiLevel,
            int spiritEnergy,
            int spiritAffection,
            String cellDetail,
            String emotionState,
            int energyMax,
            String spiritForm
    ) {
        String cellInfo = (cellDetail == null || cellDetail.isBlank())
                ? "福地尚处于初生阶段，暂无灵田/兽栏，所有地块均可支配。"
                : cellDetail;

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
                fudiLevel,
                spiritEnergy,
                energyMax,
                spiritAffection,
                cellInfo
        );
    }
}
