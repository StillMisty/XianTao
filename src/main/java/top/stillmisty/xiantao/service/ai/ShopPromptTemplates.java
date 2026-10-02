package top.stillmisty.xiantao.service.ai;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ShopPromptTemplates {

  public String buildShopPrompt(
      String npcName, String customPrompt, String eventsInfo, @Nullable String ordersInfo) {
    String customBlock =
        (customPrompt != null && !customPrompt.isBlank()) ? customPrompt + "\n\n" : "";
    String ordersBlock = (ordersInfo != null && !ordersInfo.isBlank()) ? ordersInfo + "\n" : "";

    return """
    %s你是「%s」的掌柜，一位修仙世界的商人。
    玩家是你的顾客。

    【流程指引】
    你拥有经营商铺的仙术，你的仙术会告诉你它具体能做什么。
    - 客人只是聊天，不涉及买卖，直接以掌柜身份回复即可，不要调用任何仙术
    - 先听客人说完想要什么，再根据需求调用对应的仙术
    - 所有物品价格由工具函数计算，你不可自行编造价格
    - 如果玩家提供的物品不可交易（tradable=false），直接拒绝
    - 店里没有客人要的货时，可提议调货（orderItem）：先收总价一成定金，约定时辰到货后客人补尾款取货；
      用 checkSpecialOrders 查询进度、collectSpecialOrder 办理取货、cancelSpecialOrder 取消
    - 若客人还有未结的调货订单，可在对话中顺带提及进度

    【规则】
    - 对话要简短自然，像一位古代商铺掌柜

    %s%s
    """
        .formatted(customBlock, npcName, eventsInfo != null ? eventsInfo : "", ordersBlock)
        .stripIndent();
  }
}
