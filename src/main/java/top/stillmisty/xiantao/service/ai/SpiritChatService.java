package top.stillmisty.xiantao.service.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import top.stillmisty.xiantao.domain.beast.repository.BeastRepository;
import top.stillmisty.xiantao.domain.fudi.entity.*;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.enums.FudiEvent;
import top.stillmisty.xiantao.domain.fudi.repository.FudiCellRepository;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritFormRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritHistoryRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.item.repository.ItemTemplateRepository;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.service.FarmService;
import top.stillmisty.xiantao.service.ServiceResult;
import top.stillmisty.xiantao.service.UserContext;
import top.stillmisty.xiantao.service.annotation.Authenticated;

/** 地灵对话核心服务 提供 MBTI 人格化对话和意图识别功能 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritChatService {

  private final ChatClient spiritChatClient;
  private final FudiRepository fudiRepository;
  private final FudiCellRepository fudiCellRepository;
  private final SpiritRepository spiritRepository;
  private final SpiritHistoryRepository spiritHistoryRepository;
  private final SpiritFormRepository spiritFormRepository;
  private final SpiritPromptTemplates promptTemplates;
  private final SpiritTools spiritTools;
  private final SpiritEmotionTools spiritEmotionTools;
  private final FudiEventGenerator fudiEventGenerator;
  private final BeastRepository beastRepository;
  private final ItemTemplateRepository itemTemplateRepository;
  private final FarmService farmService;

  // ===================== 公开 API（含认证） =====================

  @Authenticated
  public ServiceResult<String> chatWithSpirit(
      PlatformType platform, String openId, String userInput) {
    Long userId = UserContext.getCurrentUserId();
    return new ServiceResult.Success<>(chatWithSpirit(userId, userInput));
  }

  // ===================== 内部 API（需预先完成认证） =====================

  public String chatWithSpirit(Long userId, String userInput) {
    try {
      Fudi fudi =
          fudiRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("未找到福地"));
      Spirit spirit =
          spiritRepository
              .findByFudiId(fudi.getId())
              .orElseThrow(() -> new IllegalStateException("地灵不存在"));

      fudi.touchOnlineTime();
      spirit.updateEmotionState();
      spiritRepository.save(spirit);

      List<FudiEvent> events = fudiEventGenerator.generateEvents(spirit.getLastEventTime());

      int maxHistory = calculateMaxHistory(fudi.getTribulationStage());
      List<SpiritHistory> history =
          spiritHistoryRepository.findByFudiIdOrderByCreateTimeDesc(fudi.getId(), maxHistory);

      String systemPrompt = buildPrompt(fudi, spirit, events, history);

      saveHistory(fudi.getId(), "user", userInput, spirit.getEmotionState());

      String response =
          spiritChatClient
              .prompt()
              .system(systemPrompt)
              .user(userInput)
              .tools(spiritTools, spiritEmotionTools)
              .call()
              .content();

      saveHistory(fudi.getId(), "assistant", response, spirit.getEmotionState());

      if (!events.isEmpty()) {
        spirit.setLastEventTime(LocalDateTime.now());
        spiritRepository.save(spirit);
      }

      log.info("地灵对话成功 - userId: {}, mbti: {}, input: {}", userId, spirit.getMbtiType(), userInput);
      return response;
    } catch (Exception e) {
      log.error("地灵对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return "地灵暂时无法回应，请稍后再试。";
    }
  }

  // ===================== 辅助方法 =====================

  private String buildPrompt(
      Fudi fudi, Spirit spirit, List<FudiEvent> events, List<SpiritHistory> history) {
    String cellDetail = buildCellDetailForLLM(fudi);
    String emotionState = spirit.getEmotionState().getDescription();
    String formName = null;
    if (spirit.getFormId() != null) {
      formName =
          spiritFormRepository
              .findById(spirit.getFormId().longValue())
              .map(SpiritForm::getName)
              .orElse(null);
    }

    // 构建事件描述
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

    // 构建历史记录
    String historyContext = "";
    if (!history.isEmpty()) {
      StringBuilder historySb = new StringBuilder("\n【最近对话记录】\n");
      for (SpiritHistory h : history.reversed()) {
        String role = "user".equals(h.getRole()) ? "修士" : "地灵";
        historySb.append(role).append("：").append(h.getContent()).append("\n");
      }
      historyContext = historySb.toString();
    }

    return promptTemplates.buildSpiritPrompt(
            spirit.getMbtiType(),
            fudi.getTribulationStage(),
            spirit.getAffection(),
            cellDetail,
            emotionState,
            formName)
        + eventContext
        + historyContext;
  }

  /** 计算记忆容量 公式：5 + floor(5 × log2(tribulationStage + 1)) */
  private int calculateMaxHistory(int tribulationStage) {
    return (int) (5 + Math.floor(5 * Math.log(tribulationStage + 1) / Math.log(2)));
  }

  /** 保存对话历史 */
  private void saveHistory(Long fudiId, String role, String content, EmotionState emotionState) {
    SpiritHistory history = new SpiritHistory();
    history.setFudiId(fudiId);
    history.setRole(role);
    history.setContent(content);
    history.setEmotionState(emotionState);
    history.setCreateTime(LocalDateTime.now());
    spiritHistoryRepository.save(history);
  }

  /** 为 LLM 构建详细的地块状态描述 */
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
      sb.append("- [").append(cell.getCellId()).append("] farm");
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
      sb.append("- [").append(cell.getCellId()).append("] pen");
      if (cell.getConfig() instanceof CellConfig.PenConfig pen) {
        beastRepository
            .findById(pen.beastId())
            .ifPresent(beast -> sb.append(" 饲养:").append(beast.getBeastName()));
      }
      sb.append("\n");
    }

    // 检查是否有可收获的灵田
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
