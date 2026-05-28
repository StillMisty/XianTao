package top.stillmisty.xiantao.service.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.domain.fudi.entity.*;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.user.entity.User;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.infrastructure.repository.FudiRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritFormRepository;
import top.stillmisty.xiantao.infrastructure.repository.SpiritRepository;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.GameEventService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.activity.SubEventEffectExecutor;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.player.UserStateService;

@Service
@Slf4j
public class SpiritChatService extends AbstractChatService {

  private final FudiRepository fudiRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final SpiritPromptTemplates promptTemplates;
  private final SpiritTools spiritTools;
  private final FudiEventGenerator fudiEventGenerator;
  private final FudiStateBuilder fudiStateBuilder;
  private final SubEventEffectExecutor subEventEffectExecutor;
  private final GameEventService gameEventService;
  private final UserStateService userStateService;

  public SpiritChatService(
      ChatClient spiritChatClient,
      ChatMemory chatMemory,
      FudiRepository fudiRepository,
      SpiritRepository spiritRepository,
      SpiritFormRepository spiritFormRepository,
      SpiritPromptTemplates promptTemplates,
      SpiritTools spiritTools,
      FudiEventGenerator fudiEventGenerator,
      FudiStateBuilder fudiStateBuilder,
      SubEventEffectExecutor subEventEffectExecutor,
      GameEventService gameEventService,
      UserStateService userStateService) {
    super(spiritChatClient, chatMemory);
    this.fudiRepository = fudiRepository;
    this.spiritRepository = spiritRepository;
    this.spiritFormRepository = spiritFormRepository;
    this.promptTemplates = promptTemplates;
    this.spiritTools = spiritTools;
    this.fudiEventGenerator = fudiEventGenerator;
    this.fudiStateBuilder = fudiStateBuilder;
    this.subEventEffectExecutor = subEventEffectExecutor;
    this.gameEventService = gameEventService;
    this.userStateService = userStateService;
  }

  @Authenticated
  public ServiceResult<String> chatWithSpirit(
      PlatformType platform, String openId, String userInput) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(chatWithSpirit(userId, userInput));
  }

  public String chatWithSpirit(Long userId, String userInput) {
    try {
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

      List<FudiEventTemplate> events = fudiEventGenerator.generateEvents(spirit.getLastEventTime());

      String response =
          callLlm(
              buildPrompt(fudi, spirit, events),
              userInput,
              ChatType.SPIRIT,
              userId,
              fudi.getId(),
              spiritTools);

      if (!events.isEmpty()) {
        spirit.setLastEventTime(LocalDateTime.now());
        spiritRepository.save(spirit);
        applyFudiEventTemplateEffects(userId, events);
      }

      log.debug(
          "地灵对话成功 - userId: {}, mbti: {}, input: {}", userId, spirit.getMbtiType(), userInput);
      return response;
    } catch (BusinessException e) {
      return e.getMessage();
    } catch (Exception e) {
      log.error("地灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return "地灵暂时无法回应，请稍后再试。";
    }
  }

  private void applyFudiEventTemplateEffects(Long userId, List<FudiEventTemplate> events) {
    List<Map<String, Object>> allEffects = new ArrayList<>();
    for (FudiEventTemplate event : events) {
      if (event.hasEffects()) {
        allEffects.addAll(event.getEffects());
      }
    }
    if (allEffects.isEmpty()) return;

    try {
      User user = userStateService.loadUser(userId);
      Map<String, Object> templateArgs =
          subEventEffectExecutor.executeEffects(
              Map.of("effects", allEffects), userId, user, Map.of());

      List<GameEvent> gameEvents = new ArrayList<>();
      for (FudiEventTemplate event : events) {
        if (event.hasEffects()) {
          GameEvent gameEvent =
              GameEvent.create(userId, GameEventCategory.WORLD_EVENT)
                  .withNarrative(
                      "【" + event.getName() + "】" + event.getDescription(), templateArgs);
          gameEvents.add(gameEvent);
        }
      }
      if (!gameEvents.isEmpty()) {
        gameEventService.saveAll(gameEvents);
      }
    } catch (Exception e) {
      log.warn("应用福地事件效果失败 - userId: {}, error: {}", userId, e.getMessage());
    }
  }

  private String buildPrompt(Fudi fudi, Spirit spirit, List<FudiEventTemplate> events) {
    String cellDetail = fudiStateBuilder.buildCellDetailForLLM(fudi);
    String formName = null;
    if (spirit.getFormId() != null) {
      formName =
          spiritFormRepository.findById(spirit.getFormId()).map(SpiritForm::getName).orElse(null);
    }

    String eventContext = "";
    if (!events.isEmpty()) {
      StringBuilder eventSb = new StringBuilder("\n【最近发生的事件】\n");
      for (FudiEventTemplate event : events) {
        eventSb
            .append("- ")
            .append(event.getName())
            .append("：")
            .append(event.getDescription())
            .append("\n");
      }
      eventContext = eventSb.toString();
    }

    return promptTemplates.buildSpiritPrompt(
            spirit.getMbtiType(),
            fudi.getTribulationStage(),
            spirit.getAffection(),
            cellDetail,
            formName)
        + eventContext;
  }
}
