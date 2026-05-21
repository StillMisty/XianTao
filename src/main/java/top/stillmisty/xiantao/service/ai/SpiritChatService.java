package top.stillmisty.xiantao.service.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.*;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.enums.FudiEvent;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritFormRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;
import top.stillmisty.xiantao.service.fudi.FarmService;

@Service
@Slf4j
public class SpiritChatService extends AbstractChatService {

  private final FudiRepository fudiRepository;
  private final FudiCellRepository fudiCellRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final SpiritPromptTemplates promptTemplates;
  private final SpiritTools spiritTools;
  private final SpiritEmotionTools spiritEmotionTools;
  private final FudiEventGenerator fudiEventGenerator;
  private final BeastRepository beastRepository;
  private final FarmService farmService;

  public SpiritChatService(
      ChatClient spiritChatClient,
      ChatMemory chatMemory,
      FudiRepository fudiRepository,
      FudiCellRepository fudiCellRepository,
      SpiritRepository spiritRepository,
      SpiritFormRepository spiritFormRepository,
      SpiritPromptTemplates promptTemplates,
      SpiritTools spiritTools,
      SpiritEmotionTools spiritEmotionTools,
      FudiEventGenerator fudiEventGenerator,
      BeastRepository beastRepository,
      FarmService farmService) {
    super(spiritChatClient, chatMemory);
    this.fudiRepository = fudiRepository;
    this.fudiCellRepository = fudiCellRepository;
    this.spiritRepository = spiritRepository;
    this.spiritFormRepository = spiritFormRepository;
    this.promptTemplates = promptTemplates;
    this.spiritTools = spiritTools;
    this.spiritEmotionTools = spiritEmotionTools;
    this.fudiEventGenerator = fudiEventGenerator;
    this.beastRepository = beastRepository;
    this.farmService = farmService;
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
      if (spirit.getEmotionState() != EmotionState.EXCITED
          && spirit.getEmotionState() != EmotionState.ANGRY
          && spirit.getEmotionState() != EmotionState.EXHAUSTED) {
        spirit.updateEmotionState();
      }
      spiritRepository.save(spirit);

      List<FudiEvent> events = fudiEventGenerator.generateEvents(spirit.getLastEventTime());

      String response =
          callLlm(
              buildPrompt(fudi, spirit, events),
              userInput,
              ChatType.SPIRIT,
              userId,
              fudi.getId(),
              spiritTools,
              spiritEmotionTools);

      if (!events.isEmpty()) {
        spirit.setLastEventTime(LocalDateTime.now());
        spiritRepository.save(spirit);
      }

      log.debug(
          "地灵对话成功 - userId: {}, mbti: {}, input: {}", userId, spirit.getMbtiType(), userInput);
      return response;
    } catch (Exception e) {
      log.error("地灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return "地灵暂时无法回应，请稍后再试。";
    }
  }

  private String buildPrompt(Fudi fudi, Spirit spirit, List<FudiEvent> events) {
    String cellDetail = buildCellDetailForLLM(fudi);
    String emotionState = spirit.getEmotionState().getDescription();
    String formName = null;
    if (spirit.getFormId() != null) {
      formName =
          spiritFormRepository.findById(spirit.getFormId()).map(SpiritForm::getName).orElse(null);
    }

    String eventContext = "";
    if (!events.isEmpty()) {
      StringBuilder eventSb = new StringBuilder("\n【最近发生的事件】\n");
      for (FudiEvent event : events) {
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
            emotionState,
            formName)
        + eventContext;
  }

  private String buildCellDetailForLLM(Fudi fudi) {
    List<FudiCell> cells = fudiCellRepository.findByFudiId(fudi.getId());
    if (cells.isEmpty()) {
      return "福地尚未开辟任何地块。";
    }

    int totalCells = cells.size();
    List<FudiCell> emptyCells = new ArrayList<>();
    List<FudiCell> farmCells = new ArrayList<>();
    List<FudiCell> penCells = new ArrayList<>();

    for (FudiCell cell : cells) {
      switch (cell.getCellType()) {
        case EMPTY -> emptyCells.add(cell);
        case FARM -> farmCells.add(cell);
        case PEN -> penCells.add(cell);
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append("福地状态（共").append(totalCells).append("个地块）：\n");

    List<String> typeSummary = new ArrayList<>();
    if (!farmCells.isEmpty()) typeSummary.add("灵田×" + farmCells.size());
    if (!penCells.isEmpty()) typeSummary.add("兽栏×" + penCells.size());
    if (!typeSummary.isEmpty()) {
      sb.append("地块组成：").append(String.join("、", typeSummary)).append("\n");
    }

    if (emptyCells.isEmpty()) {
      sb.append("所有地块已使用。如需调整布局可先拆除部分地块。\n");
    } else {
      sb.append("可用空地块编号：");
      sb.append(emptyCells.stream().map(c -> String.valueOf(c.getCellId())).toList());
      sb.append("\n");
    }

    sb.append("【已占地块详情】\n");
    for (FudiCell cell : farmCells) {
      sb.append("- [").append(cell.getCellId()).append("] FARM");
      if (cell.getConfig() instanceof CellConfig.FarmConfig farm) {
        sb.append(" 种植:").append(farmService.getCropName(farm.cropId()));
        Double progress = farmService.calculateGrowthProgress(cell);
        if (progress != null) {
          if (progress >= 1.0) {
            sb.append(" 可收获✅");
          } else {
            sb.append(String.format(" (%.0f%%)", progress * 100));
          }
        }
      }
      sb.append("\n");
    }

    for (FudiCell cell : penCells) {
      sb.append("- [").append(cell.getCellId()).append("] PEN");
      if (cell.getConfig() instanceof CellConfig.PenConfig pen) {
        beastRepository
            .findById(pen.beastId())
            .ifPresent(beast -> sb.append(" 饲养:").append(beast.getBeastName()));
      }
      sb.append("\n");
    }

    long matureFarmCount =
        farmCells.stream()
            .filter(
                c -> {
                  if (c.getConfig() instanceof CellConfig.FarmConfig) {
                    Double progress = farmService.calculateGrowthProgress(c);
                    return progress != null && progress >= 1.0;
                  }
                  return false;
                })
            .count();
    if (matureFarmCount > 0) {
      sb.append("有 ").append(matureFarmCount).append(" 块灵田可收获。\n");
    }

    return sb.toString();
  }
}
