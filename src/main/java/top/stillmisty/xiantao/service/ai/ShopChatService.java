package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.repository.WorldEventRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;
import top.stillmisty.xiantao.service.shop.ShopService;

@Slf4j
@Service
public class ShopChatService extends AbstractChatService {

  private final ShopService shopService;
  private final ShopTools shopTools;
  private final UserStateService userStateService;
  private final WorldEventRepository worldEventRepository;

  public ShopChatService(
      ChatClient shopChatClient,
      ChatMemory chatMemory,
      ShopService shopService,
      ShopTools shopTools,
      UserStateService userStateService,
      WorldEventRepository worldEventRepository) {
    super(shopChatClient, chatMemory);
    this.shopService = shopService;
    this.shopTools = shopTools;
    this.userStateService = userStateService;
    this.worldEventRepository = worldEventRepository;
  }

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
      List<WorldEvent> activeEvents = worldEventRepository.findActiveEvents();
      return ShopChatContext.with(
          user,
          npc,
          activeEvents,
          () ->
              callLlm(buildPrompt(npc), userInput, ChatType.SHOP, userId, npc.getId(), shopTools));
    } catch (BusinessException e) {
      return e.getMessage();
    } catch (Exception e) {
      log.error("商铺对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return "掌柜暂时不在，请稍后再来。";
    }
  }

  private String buildPrompt(ShopNpc npc) {
    String customPrompt =
        (npc.getSystemPrompt() != null && !npc.getSystemPrompt().isBlank())
            ? npc.getSystemPrompt() + "\n\n"
            : "";

    String eventsInfo = buildEventsInfo();

    String promptTemplate =
        """
            %s你是「%s」的掌柜，一位修仙世界的商人。
            玩家是你的顾客，你的职责是收购和出售物品，不能进行修炼、战斗、炼丹等操作。
            所有物品价格由工具函数计算，你不可自行编造价格。
            当客人要求降价或抬价时，必须调用 negotiatePrice 工具，不要自己口头拒绝或接受。
            isBuying=true 表示客人在买东西想降价，isBuying=false 表示客人在卖东西想提价。
            如果玩家提供的物品不可交易（tradable=false），直接拒绝。
            对话要简短自然，像一位古代商铺掌柜。
            如果玩家只是聊天，不涉及买卖，直接以掌柜身份回复即可，不要调用任何工具。

            %s
            """
            .stripIndent();

    return String.format(promptTemplate, customPrompt, npc.getName(), eventsInfo);
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
