package top.stillmisty.xiantao.service.ai;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.infrastructure.repository.FudiRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritFormRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritRepository;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.player.UserStateService;

@Service
@Slf4j
public class SpiritChatService extends AbstractChatService {

  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final WorldEventRepository worldEventRepository;
  private final UserStateService userStateService;
  private final SpiritPromptTemplates promptTemplates;
  private final SpiritCellTools spiritCellTools;
  private final SpiritBeastTools spiritBeastTools;
  private final SpiritInteractionTools spiritInteractionTools;
  private final FudiStateBuilder fudiStateBuilder;
  private final AiChatRateLimiter rateLimiter;

  public SpiritChatService(
      ChatClient spiritChatClient,
      ChatMemory chatMemory,
      FudiRepository fudiRepository,
      SpiritRepository spiritRepository,
      SpiritFormRepository spiritFormRepository,
      WorldEventRepository worldEventRepository,
      UserStateService userStateService,
      SpiritPromptTemplates promptTemplates,
      SpiritCellTools spiritCellTools,
      SpiritBeastTools spiritBeastTools,
      SpiritInteractionTools spiritInteractionTools,
      FudiStateBuilder fudiStateBuilder,
      AiChatRateLimiter rateLimiter) {
    super(spiritChatClient, chatMemory);
    this.fudiRepository = fudiRepository;
    this.spiritRepository = spiritRepository;
    this.spiritFormRepository = spiritFormRepository;
    this.worldEventRepository = worldEventRepository;
    this.userStateService = userStateService;
    this.promptTemplates = promptTemplates;
    this.spiritCellTools = spiritCellTools;
    this.spiritBeastTools = spiritBeastTools;
    this.spiritInteractionTools = spiritInteractionTools;
    this.fudiStateBuilder = fudiStateBuilder;
    this.rateLimiter = rateLimiter;
  }

  public ServiceResult<String> chatWithSpirit(Long userId, String userInput) {
    rateLimiter.checkAllowed(userId);
    try {
      String result = chatWithSpiritInternal(userId, userInput);
      return new ServiceResult.Success<>(result != null ? result : "地灵暂时无法回应，请稍后再试。");
    } catch (BusinessException e) {
      return ServiceResult.businessFailure(e.getMessage() != null ? e.getMessage() : "地灵操作失败");
    } catch (Exception e) {
      log.error("地灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return ServiceResult.businessFailure("地灵暂时无法回应，请稍后再试。");
    }
  }

  @Nullable String chatWithSpiritInternal(Long userId, String userInput) {
    Fudi fudi =
        fudiRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND));
    Spirit spirit =
        spiritRepository
            .findByFudiId(fudi.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SPIRIT_NOT_FOUND));

    fudi.touchOnlineTime();
    spiritRepository.save(spirit);

    List<WorldEvent> activeEvents = loadVisibleEvents(userId);
    String response =
        SpiritChatContext.with(
            fudi,
            spirit,
            activeEvents,
            () ->
                callLlm(
                    buildPrompt(fudi, spirit),
                    userInput,
                    ChatType.SPIRIT,
                    userId,
                    fudi.getId(),
                    spiritCellTools,
                    spiritBeastTools,
                    spiritInteractionTools));

    log.debug("地灵对话成功 - userId: {}, mbti: {}, input: {}", userId, spirit.getMbtiType(), userInput);
    return response;
  }

  /** 加载玩家所在位置可见的进行中事件（全局 + 本地区域），供叙事上下文注入。 */
  private List<WorldEvent> loadVisibleEvents(Long userId) {
    Long locationId = userStateService.loadUserReadOnly(userId).getLocationId();
    List<WorldEvent> events =
        new ArrayList<>(worldEventRepository.findActiveByScope(WorldEventScope.GLOBAL));
    if (locationId != null) {
      events.addAll(worldEventRepository.findActiveByRegion(locationId));
    }
    return events;
  }

  private String buildPrompt(Fudi fudi, Spirit spirit) {
    String cellDetail = fudiStateBuilder.buildCellDetailForLLM(fudi);
    String formName = null;
    if (spirit.getFormId() != null) {
      formName =
          spiritFormRepository.findById(spirit.getFormId()).map(SpiritForm::getName).orElse(null);
    }

    return promptTemplates.buildSpiritPrompt(
        spirit.getMbtiType(),
        fudi.getTribulationStage(),
        spirit.getAffection(),
        cellDetail,
        formName != null ? formName : "未知形态",
        buildEventsInfo());
  }

  /** 进行中叙事事件上下文 — 与 ShopChatService 的注入方式一致，仅注入 NARRATIVE 类事件。 */
  private String buildEventsInfo() {
    SpiritChatContext ctx = SpiritChatContext.current();
    List<WorldEvent> activeEvents = ctx != null ? ctx.activeEvents() : List.of();
    List<WorldEvent> narrativeEvents =
        activeEvents.stream()
            .filter(event -> event.getCategory() == WorldEventCategory.NARRATIVE)
            .toList();
    if (narrativeEvents.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder("当前世界事件（叙事见闻，可在对话中自然提及）：\n");
    for (WorldEvent event : narrativeEvents) {
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
