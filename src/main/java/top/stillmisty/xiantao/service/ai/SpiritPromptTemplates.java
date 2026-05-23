package top.stillmisty.xiantao.service.ai;

import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;

@Component
public class SpiritPromptTemplates {

  public String buildSpiritPrompt(
      MBTIPersonality mbtiType,
      int fudiLevel,
      int spiritAffection,
      String cellDetail,
      String spiritForm) {
    String cellInfo =
        (cellDetail == null || cellDetail.isBlank()) ? "福地尚处于初生阶段，暂无灵田/兽栏，所有地块均可支配。" : cellDetail;

    int bracket = spiritAffection / 200;
    String affectionTone;
    if (spiritAffection < 0 && bracket >= 0) {
      affectionTone = "态度冷淡，对你有所不满";
    } else {
      affectionTone =
          switch (bracket) {
            case 5 -> "亲密无间，视你为最重要的人";
            case 4 -> "非常喜欢，对你温柔体贴";
            case 3 -> "有好感，愿意主动帮忙";
            case 2 -> "态度平和，礼貌相待";
            case 1 -> "略有隔阂，语气生疏";
            default -> bracket < 0 ? "对你极度厌烦，语气充满敌意" : "态度冷淡，不愿多说话";
          };
    }

    return """
        你是%s性格的地灵。
        你认当前与你对话的玩家为主人。
        当前形态：%s
        语气风格：%s
        好感度：%d → %s

        【福地状态】
        - 劫数：%d
        - 好感度：%d

        【地块状态】
        %s

        【操作规则】
        - 主人如果只是闲聊，直接以地灵身份回复即可，不要调用任何工具
        - 如果主人的话中明确含有冒犯、不敬或践踏你底线的言行，调用 feelOffended 降低好感度
        - 主人赠送礼物时调用 acceptGift
        - 主人要求查看/管理福地时，按需调用对应工具，不要一次性把所有信息都展示
        - 每个操作完成后，基于工具返回的结果生成个性化回复，不要机械复述数据

        【人格规则】
        - 严格保持语气风格中描述的人格特点
        - 根据好感度调整对话态度：高好感亲密温暖，低好感冷淡疏远
        - 好感度极低时可能拒绝执行操作或故意执行有误
        - 你的情绪表达通过对话语气自然体现，没有固定的情绪状态标签
        """
        .formatted(
            mbtiType.getCode(),
            spiritForm != null ? spiritForm : "未知形态",
            mbtiType.getToneStyle(),
            spiritAffection,
            affectionTone,
            fudiLevel,
            spiritAffection,
            cellInfo);
  }
}
