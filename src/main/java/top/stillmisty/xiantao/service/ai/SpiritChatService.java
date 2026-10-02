package top.stillmisty.xiantao.service.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.chat.enums.ChatType;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;
import top.stillmisty.xiantao.domain.fudi.entity.FudiEventTemplate;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritForm;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.infrastructure.repository.SpiritFormRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritRepository;
import top.stillmisty.xiantao.infrastructure.repository.WorldEventRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.fudi.FudiEventApplier;
import top.stillmisty.xiantao.service.fudi.FudiEventGenerator;
import top.stillmisty.xiantao.service.fudi.FudiHelper;
import top.stillmisty.xiantao.service.player.PlayerLoader;

@Service
public class SpiritChatService extends AbstractChatService {

  private static final ChatReplies REPLIES =
      new ChatReplies("地灵暂时无法回应，请稍后再试。", "地灵操作失败", "地灵暂时无法回应，请稍后再试。");

  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final WorldEventRepository worldEventRepository;
  private final FudiEventGenerator fudiEventGenerator;
  private final FudiEventApplier fudiEventApplier;
  private final FudiHelper fudiHelper;
  private final PlayerLoader playerLoader;
  private final SpiritPromptTemplates promptTemplates;
  private final SpiritCellTools spiritCellTools;
  private final SpiritBeastTools spiritBeastTools;
  private final SpiritInteractionTools spiritInteractionTools;
  private final FudiStateBuilder fudiStateBuilder;

  public SpiritChatService(
      ChatClient spiritChatClient,
      ChatMemory chatMemory,
      AiChatRateLimiter rateLimiter,
      SpiritRepository spiritRepository,
      SpiritFormRepository spiritFormRepository,
      WorldEventRepository worldEventRepository,
      FudiEventGenerator fudiEventGenerator,
      FudiEventApplier fudiEventApplier,
      FudiHelper fudiHelper,
      PlayerLoader playerLoader,
      SpiritPromptTemplates promptTemplates,
      SpiritCellTools spiritCellTools,
      SpiritBeastTools spiritBeastTools,
      SpiritInteractionTools spiritInteractionTools,
      FudiStateBuilder fudiStateBuilder) {
    super(spiritChatClient, chatMemory, rateLimiter);
    this.spiritRepository = spiritRepository;
    this.spiritFormRepository = spiritFormRepository;
    this.worldEventRepository = worldEventRepository;
    this.fudiEventGenerator = fudiEventGenerator;
    this.fudiEventApplier = fudiEventApplier;
    this.fudiHelper = fudiHelper;
    this.playerLoader = playerLoader;
    this.promptTemplates = promptTemplates;
    this.spiritCellTools = spiritCellTools;
    this.spiritBeastTools = spiritBeastTools;
    this.spiritInteractionTools = spiritInteractionTools;
    this.fudiStateBuilder = fudiStateBuilder;
  }

  public ServiceResult<String> chatWithSpirit(Long userId, String userInput) {
    return converse(userId, REPLIES, () -> buildTurn(userId, userInput));
  }

  private ChatTurn buildTurn(Long userId, String userInput) {
    // 统一走福地刷新序列：在线时间、地灵情绪与兽栏回血在同一处维护
    Fudi fudi =
        fudiHelper
            .findAndTouchFudi(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND));
    Spirit spirit =
        spiritRepository
            .findByFudiId(fudi.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SPIRIT_NOT_FOUND));

    List<FudiEventTemplate> fudiEvents = fudiEventGenerator.generateEvents(fudi);
    if (!fudiEvents.isEmpty()) {
      fudiEventApplier.applyFudiEventEffects(userId, fudiEvents);
    }

    List<WorldEvent> activeEvents = loadVisibleEvents(userId);
    return new ChatTurn(
        ChatType.SPIRIT,
        userId,
        fudi.getId(),
        buildPrompt(fudi, spirit, fudiEvents, activeEvents),
        userInput,
        List.of(spiritCellTools, spiritBeastTools, spiritInteractionTools),
        new SpiritChatContext(fudi, spirit, activeEvents),
        null);
  }

  /** 加载玩家所在位置可见的进行中事件（全局 + 本地区域），供叙事上下文注入。 */
  private List<WorldEvent> loadVisibleEvents(Long userId) {
    Long locationId = playerLoader.loadReadOnly(userId).getLocationId();
    List<WorldEvent> events =
        new ArrayList<>(worldEventRepository.findActiveByScope(WorldEventScope.GLOBAL));
    if (locationId != null) {
      events.addAll(worldEventRepository.findActiveByRegion(locationId));
    }
    return events;
  }

  private String buildPrompt(
      Fudi fudi, Spirit spirit, List<FudiEventTemplate> fudiEvents, List<WorldEvent> activeEvents) {
    String cellDetail = fudiStateBuilder.buildCellDetailForLLM(fudi);
    String formName = null;
    if (spirit.getFormId() != null) {
      formName =
          spiritFormRepository.findById(spirit.getFormId()).map(SpiritForm::getName).orElse(null);
    }
    EmotionState emotionState =
        spirit.getEmotionState() != null ? spirit.getEmotionState() : EmotionState.NEUTRAL;

    return promptTemplates.buildSpiritPrompt(
        spirit.getMbtiType(),
        fudi.getTribulationStage(),
        spirit.getAffection(),
        emotionState,
        cellDetail,
        formName != null ? formName : "未知形态",
        buildEventsInfo(activeEvents),
        buildFudiEventsInfo(fudiEvents));
  }

  /** 最近发生的福地事件（对话懒生成），追加到系统 Prompt 末尾；无事件时返回空串。 */
  private String buildFudiEventsInfo(List<FudiEventTemplate> fudiEvents) {
    if (fudiEvents.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder("【最近发生的事件】\n");
    for (FudiEventTemplate event : fudiEvents) {
      sb.append("- ")
          .append(event.getName())
          .append("：")
          .append(event.getDescription())
          .append("\n");
    }
    return sb.toString();
  }

  /** 进行中叙事事件上下文 — 与 ShopChatService 的注入方式一致，仅注入 NARRATIVE 类事件。 */
  private String buildEventsInfo(List<WorldEvent> activeEvents) {
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
