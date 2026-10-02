package top.stillmisty.xiantao.service.ai;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.chat.enums.ChatType;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.PlayerLoader;
import top.stillmisty.xiantao.service.shop.ShopService;
import top.stillmisty.xiantao.service.shop.SpecialOrderService;

@Service
public class ShopChatService extends AbstractChatService {

  private static final ChatReplies REPLIES =
      new ChatReplies("掌柜暂时不在，请稍后再来。", "商铺操作失败", "掌柜暂时不在，请稍后再来。");

  private final ShopService shopService;
  private final ShopTools shopTools;
  private final SpecialOrderService specialOrderService;
  private final PlayerLoader playerLoader;
  private final WorldEventRepository worldEventRepository;
  private final ShopPromptTemplates promptTemplates;

  public ShopChatService(
      ChatClient shopChatClient,
      ChatMemory chatMemory,
      AiChatRateLimiter rateLimiter,
      ShopService shopService,
      ShopTools shopTools,
      SpecialOrderService specialOrderService,
      PlayerLoader playerLoader,
      WorldEventRepository worldEventRepository,
      ShopPromptTemplates promptTemplates) {
    super(shopChatClient, chatMemory, rateLimiter);
    this.shopService = shopService;
    this.shopTools = shopTools;
    this.specialOrderService = specialOrderService;
    this.playerLoader = playerLoader;
    this.worldEventRepository = worldEventRepository;
    this.promptTemplates = promptTemplates;
  }

  public ServiceResult<String> chatWithShopkeeper(Long userId, String userInput) {
    return converse(userId, REPLIES, () -> buildTurn(userId, userInput));
  }

  private ChatTurn buildTurn(Long userId, String userInput) {
    Player user = playerLoader.load(userId);
    ShopNpc npc = shopService.findByLocation(user.getLocationId());
    List<WorldEvent> activeEvents = worldEventRepository.findActiveEvents();
    String prompt =
        promptTemplates.buildShopPrompt(
            npc.getName(),
            npc.getSystemPrompt(),
            buildEventsInfo(activeEvents),
            specialOrderService.describeOrdersForPrompt(user.getId(), npc.getId()));
    return new ChatTurn(
        ChatType.SHOP,
        userId,
        npc.getId(),
        prompt,
        userInput,
        List.of(shopTools),
        new ShopChatContext(user, npc, activeEvents),
        null);
  }

  private static String buildEventsInfo(List<WorldEvent> activeEvents) {
    if (activeEvents.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder("当前世界事件（可能影响物品价格或其它方面）：\n");
    for (WorldEvent event : activeEvents) {
      sb.append("- [")
          .append(event.getCategory().getName())
          .append("] ")
          .append(event.getTitle())
          .append("：")
          .append(event.getDescription())
          .append("\n");
    }
    return sb.toString();
  }
}
