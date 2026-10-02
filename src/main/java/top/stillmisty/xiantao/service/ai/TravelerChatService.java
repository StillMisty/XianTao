package top.stillmisty.xiantao.service.ai;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.chat.enums.ChatType;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.PlayerLoader;
import top.stillmisty.xiantao.service.shop.TravelerShopService;

/** 旅行商人对话 — 仅在临时摊位有效期内可用（会话由旅行商人选择事件开启） */
@Service
public class TravelerChatService extends AbstractChatService {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

  private static final ChatReplies REPLIES =
      new ChatReplies("旅行商人摆了摆手：「货源就这些，道友慢慢看。」", "旅行商人已经离开了", "旅行商人已经离开了。");

  private final TravelerShopService travelerShopService;
  private final TravelerTools travelerTools;
  private final TravelerPromptTemplates promptTemplates;
  private final PlayerLoader playerLoader;

  public TravelerChatService(
      ChatClient shopChatClient,
      ChatMemory chatMemory,
      AiChatRateLimiter rateLimiter,
      TravelerShopService travelerShopService,
      TravelerTools travelerTools,
      TravelerPromptTemplates promptTemplates,
      PlayerLoader playerLoader) {
    super(shopChatClient, chatMemory, rateLimiter);
    this.travelerShopService = travelerShopService;
    this.travelerTools = travelerTools;
    this.promptTemplates = promptTemplates;
    this.playerLoader = playerLoader;
  }

  public ServiceResult<String> chatWithTraveler(Long userId, String userInput) {
    return converse(userId, REPLIES, () -> buildTurn(userId, userInput));
  }

  private ChatTurn buildTurn(Long userId, String userInput) {
    TravelerShopService.TravelerSession session = travelerShopService.requireSession(userId);
    Player user = playerLoader.loadReadOnly(userId);
    String prompt =
        promptTemplates.buildTravelerPrompt(
            session.merchantName(), user.getNickname(), TIME_FORMAT.format(session.expiresAt()));
    return new ChatTurn(
        ChatType.TRAVELER,
        userId,
        session.sessionId(),
        prompt,
        userInput,
        List.of(travelerTools),
        null,
        null);
  }
}
