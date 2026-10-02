package top.stillmisty.xiantao.service.ai;

import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.user.entity.Player;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.PlayerLoader;
import top.stillmisty.xiantao.service.shop.TravelerShopService;

/** 旅行商人对话 — 仅在临时摊位有效期内可用（会话由旅行商人选择事件开启） */
@Slf4j
@Service
public class TravelerChatService extends AbstractChatService {

  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

  private final TravelerShopService travelerShopService;
  private final TravelerTools travelerTools;
  private final TravelerPromptTemplates promptTemplates;
  private final PlayerLoader playerLoader;
  private final AiChatRateLimiter rateLimiter;

  public TravelerChatService(
      ChatClient shopChatClient,
      ChatMemory chatMemory,
      TravelerShopService travelerShopService,
      TravelerTools travelerTools,
      TravelerPromptTemplates promptTemplates,
      PlayerLoader playerLoader,
      AiChatRateLimiter rateLimiter) {
    super(shopChatClient, chatMemory);
    this.travelerShopService = travelerShopService;
    this.travelerTools = travelerTools;
    this.promptTemplates = promptTemplates;
    this.playerLoader = playerLoader;
    this.rateLimiter = rateLimiter;
  }

  public ServiceResult<String> chatWithTraveler(Long userId, String userInput) {
    rateLimiter.checkAllowed(userId);
    try {
      String result = chatWithTravelerInternal(userId, userInput);
      return new ServiceResult.Success<>(result != null ? result : "商人笑而不语，只顾拨弄算盘。");
    } catch (BusinessException e) {
      return ServiceResult.businessFailure(e.getMessage() != null ? e.getMessage() : "旅行商人已经离开了");
    } catch (Exception e) {
      log.error("旅行商人对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return ServiceResult.businessFailure("旅行商人已经离开了。");
    }
  }

  String chatWithTravelerInternal(Long userId, String userInput) {
    TravelerShopService.TravelerSession session = travelerShopService.requireSession(userId);
    Player user = playerLoader.loadReadOnly(userId);
    String prompt =
        promptTemplates.buildTravelerPrompt(
            session.merchantName(), user.getNickname(), TIME_FORMAT.format(session.expiresAt()));
    String reply =
        callLlm(prompt, userInput, ChatType.TRAVELER, userId, session.sessionId(), travelerTools);
    return reply != null ? reply : "旅行商人摆了摆手：「货源就这些，道友慢慢看。」";
  }
}
