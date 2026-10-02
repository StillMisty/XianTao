package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.UserStateService;
import top.stillmisty.xiantao.service.shop.ShopService;
import top.stillmisty.xiantao.service.shop.SpecialOrderService;

@Slf4j
@Service
public class ShopChatService extends AbstractChatService {

  private final ShopService shopService;
  private final ShopTools shopTools;
  private final SpecialOrderService specialOrderService;
  private final UserStateService userStateService;
  private final WorldEventRepository worldEventRepository;

  private final ShopPromptTemplates promptTemplates;
  private final AiChatRateLimiter rateLimiter;

  public ShopChatService(
      ChatClient shopChatClient,
      ChatMemory chatMemory,
      ShopService shopService,
      ShopTools shopTools,
      SpecialOrderService specialOrderService,
      UserStateService userStateService,
      WorldEventRepository worldEventRepository,
      ShopPromptTemplates promptTemplates,
      AiChatRateLimiter rateLimiter) {
    super(shopChatClient, chatMemory);
    this.shopService = shopService;
    this.shopTools = shopTools;
    this.specialOrderService = specialOrderService;
    this.userStateService = userStateService;
    this.worldEventRepository = worldEventRepository;
    this.promptTemplates = promptTemplates;
    this.rateLimiter = rateLimiter;
  }

  public ServiceResult<String> chatWithShopkeeper(Long userId, String userInput) {
    rateLimiter.checkAllowed(userId);
    try {
      String result = chatWithShopkeeperInternal(userId, userInput);
      return new ServiceResult.Success<>(result != null ? result : "掌柜暂时不在，请稍后再来。");
    } catch (BusinessException e) {
      return ServiceResult.businessFailure(e.getMessage() != null ? e.getMessage() : "商铺操作失败");
    } catch (Exception e) {
      log.error("商铺对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return ServiceResult.businessFailure("掌柜暂时不在，请稍后再来。");
    }
  }

  String chatWithShopkeeperInternal(Long userId, String userInput) {
    Player user = userStateService.loadUser(userId);
    ShopNpc npc = shopService.findByLocation(user.getLocationId());
    List<WorldEvent> activeEvents = worldEventRepository.findActiveEvents();
    return ShopChatContext.with(
        user,
        npc,
        activeEvents,
        () ->
            callLlm(
                buildPrompt(npc, user), userInput, ChatType.SHOP, userId, npc.getId(), shopTools));
  }

  private String buildPrompt(ShopNpc npc, Player user) {
    String eventsInfo = buildEventsInfo();
    String ordersInfo = specialOrderService.describeOrdersForPrompt(user.getId(), npc.getId());
    return promptTemplates.buildShopPrompt(
        npc.getName(), npc.getSystemPrompt(), eventsInfo, ordersInfo);
  }

  private String buildEventsInfo() {
    ShopChatContext ctx = ShopChatContext.current();
    List<WorldEvent> activeEvents =
        ctx != null ? ctx.activeEvents() : worldEventRepository.findActiveEvents();
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
