package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.entity.SpiritHistory;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritHistoryRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.service.UserContext;

/** 地灵情绪工具 供 LLM 通过 Function Calling 自主判断和更新情绪状态 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpiritEmotionTools {

  private final SpiritRepository spiritRepository;
  private final FudiRepository fudiRepository;
  private final SpiritHistoryRepository spiritHistoryRepository;

  /** 更新地灵情绪状态 */
  @Tool(description = "更新地灵的情绪状态")
  @Transactional
  public String updateEmotion(@ToolParam(description = "新的情绪状态") EmotionState emotionState) {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
      return "无法获取用户信息";
    }

    try {
      Spirit spirit =
          spiritRepository
              .findByFudiId(getFudiId(userId))
              .orElseThrow(() -> new IllegalStateException("地灵不存在"));

      spirit.setEmotionState(emotionState);
      spiritRepository.save(spirit);

      log.info("地灵情绪更新 - userId: {}, emotion: {}", userId, emotionState);
      return "情绪已更新为：" + emotionState.getDescription();
    } catch (Exception e) {
      log.error("更新情绪失败 - userId: {}, error: {}", userId, e.getMessage());
      return "更新情绪失败：" + e.getMessage();
    }
  }

  /** 添加地灵想法/记忆 */
  @Tool(description = "记录地灵的想法或重要事件到记忆中")
  @Transactional
  public String addThought(@ToolParam(description = "要记录的想法或事件内容") String thought) {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
      return "无法获取用户信息";
    }

    try {
      Long fudiId = getFudiId(userId);
      SpiritHistory history = new SpiritHistory();
      history.setFudiId(fudiId);
      history.setRole("system");
      history.setContent(thought);
      spiritHistoryRepository.save(history);

      log.info("地灵想法已记录 - userId: {}, thought: {}", userId, thought);
      return "想法已记录";
    } catch (Exception e) {
      log.error("记录想法失败 - userId: {}, error: {}", e.getMessage());
      return "记录想法失败：" + e.getMessage();
    }
  }

  private Long getFudiId(Long userId) {
    return fudiRepository
        .findByUserId(userId)
        .orElseThrow(() -> new IllegalStateException("未找到福地"))
        .getId();
  }
}
