package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.sect.entity.ChatHistory;
import top.stillmisty.xiantao.domain.sect.enums.ChatRole;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.sect.repository.ChatHistoryRepository;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;
import top.stillmisty.xiantao.domain.shop.entity.WorldEvent;
import top.stillmisty.xiantao.domain.shop.repository.ShopProductRepository;
import top.stillmisty.xiantao.domain.shop.repository.WorldEventRepository;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;
import top.stillmisty.xiantao.service.shop.ShopService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopChatService {

  private final ChatClient npcChatClient;
  private final ChatMemory chatMemory;
  private final ShopService shopService;
  private final ShopTools shopTools;
  private final UserStateService userStateService;
  private final ChatHistoryRepository chatHistoryRepository;
  private final ShopProductRepository shopProductRepository;
  private final WorldEventRepository worldEventRepository;

  @Authenticated
  public ServiceResult<String> chatWithShopkeeper(
      PlatformType platform, String openId, String userInput) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(chatWithShopkeeper(userId, userInput));
  }

  public String chatWithShopkeeper(Long userId, String userInput) {
    try {
      User user = userStateService.loadUser(userId);
      ShopNpc npc = shopService.findByLocation(user.getLocationId());

      String systemPrompt = buildPrompt(npc);
      String conversationId = "shop:" + userId + ":" + npc.getId();

      saveHistory(userId, npc.getId(), ChatRole.USER, userInput);

      String response =
          npcChatClient
              .prompt()
              .system(systemPrompt)
              .user(userInput)
              .tools(shopTools)
              .advisors(
                  a ->
                      a.param(ChatMemory.CONVERSATION_ID, conversationId)
                          .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build()))
              .call()
              .content();

      saveHistory(userId, npc.getId(), ChatRole.ASSISTANT, response);

      log.debug("商铺对话成功 - userId: {}, shopNpc: {}, input: {}", userId, npc.getName(), userInput);
      return response;
    } catch (BusinessException e) {
      return e.getMessage();
    } catch (Exception e) {
      log.error("商铺对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return "掌柜暂时不在，请稍后再来。";
    }
  }

  private String buildPrompt(ShopNpc npc) {
    StringBuilder sb = new StringBuilder();

    if (npc.getSystemPrompt() != null && !npc.getSystemPrompt().isBlank()) {
      sb.append(npc.getSystemPrompt()).append("\n\n");
    }

    sb.append("你是一个修仙世界的商铺掌柜。\n");
    sb.append("你只能收购和出售物品，不能进行其他操作（如修炼、战斗、炼丹等）。\n");
    sb.append("所有物品的价格由系统函数计算，你不能自己编造价格。\n");
    sb.append("同一笔交易中，玩家最多只能砍一次价——如果玩家第二次要求降价，请拒绝。\n");
    sb.append("首次报价必须等于估价工具返回的 basePrice，不能高于或低于它。\n");
    sb.append("如果玩家提供的物品不可回收（tradable=false），直接拒绝。\n");
    sb.append("对话要简短自然，像一位古代商铺掌柜。\n");

    List<ShopProduct> products = shopProductRepository.findByShopNpcId(npc.getId());
    if (!products.isEmpty()) {
      sb.append("\n当前在售商品（仅供参考，实际以工具查询为准）：\n");
      sb.append("商品数量：").append(products.size()).append(" 种\n");
    }

    List<WorldEvent> activeEvents = worldEventRepository.findActiveEvents();
    if (!activeEvents.isEmpty()) {
      sb.append("\n当前世界事件（可能影响物品价格）：\n");
      for (WorldEvent event : activeEvents) {
        sb.append("- ")
            .append(event.getTitle())
            .append("：")
            .append(event.getDescription())
            .append("\n");
      }
    }

    return sb.toString();
  }

  private void saveHistory(Long userId, Long npcId, ChatRole role, String content) {
    ChatHistory history = new ChatHistory();
    history.setChatType(ChatType.SHOP);
    history.setConversationId(npcId);
    history.setUserId(userId);
    history.setRole(role);
    history.setContent(content);
    chatHistoryRepository.save(history);
  }
}
