package top.stillmisty.xiantao.service.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.UserContext;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpiritEmotionTools {

  private final SpiritRepository spiritRepository;
  private final FudiRepository fudiRepository;
  private final ChatMemory chatMemory;

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
              .orElseThrow(() -> new BusinessException(ErrorCode.SPIRIT_NOT_FOUND));

      spirit.setEmotionState(emotionState);
      spiritRepository.save(spirit);

      log.debug("地灵情绪更新 - userId: {}, emotion: {}", userId, emotionState);
      return "情绪已更新为：" + emotionState.getDescription();
    } catch (Exception e) {
      log.warn("更新情绪失败 - userId: {}, error: {}", userId, e.getMessage());
      return "更新情绪失败：" + e.getMessage();
    }
  }

  @Tool(description = "记录地灵的想法或重要事件到记忆中")
  @Transactional
  public String addThought(@ToolParam(description = "要记录的想法或事件内容") String thought) {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
      return "无法获取用户信息";
    }

    try {
      Long fudiId = getFudiId(userId);
      String conversationId = new ConversationId(ChatType.SPIRIT, userId, fudiId).value();
      chatMemory.add(conversationId, List.of(new SystemMessage(thought)));

      log.debug("地灵想法已记录 - userId: {}, thought: {}", userId, thought);
      return "想法已记录";
    } catch (Exception e) {
      log.warn("记录想法失败 - userId: {}, error: {}", userId, e.getMessage());
      return "记录想法失败：" + e.getMessage();
    }
  }

  private Long getFudiId(Long userId) {
    return fudiRepository
        .findByUserId(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND))
        .getId();
  }
}
