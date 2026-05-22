package top.stillmisty.xiantao.service.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.repository.FudiRepository;
import top.stillmisty.xiantao.domain.fudi.repository.SpiritRepository;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ErrorCode;
import top.stillmisty.xiantao.service.UserContext;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpiritEmotionTools {

  private final SpiritRepository spiritRepository;
  private final FudiRepository fudiRepository;
  private final ChatMemory chatMemory;

  @Tool(description = "更新地灵的情绪状态")
  @Transactional
  public UpdateEmotionResponse updateEmotion(
      @ToolParam(description = "新的情绪状态") EmotionState emotionState) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      Spirit spirit =
          spiritRepository
              .findByFudiId(getFudiId(userId))
              .orElseThrow(() -> new BusinessException(ErrorCode.SPIRIT_NOT_FOUND));

      spirit.setEmotionState(emotionState);
      spiritRepository.save(spirit);

      log.debug("地灵情绪更新 - userId: {}, emotion: {}", userId, emotionState);
      return new UpdateEmotionResponse(
          true, "情绪已更新为：" + emotionState.getDescription(), emotionState.name());
    } catch (Exception e) {
      log.error("更新情绪失败 - error: {}", e.getMessage());
      return new UpdateEmotionResponse(false, "更新情绪失败：" + e.getMessage(), "");
    }
  }

  @Tool(description = "记录地灵的想法或事件到短期记忆，影响后续对话")
  @Transactional
  public AddThoughtResponse addThought(@ToolParam(description = "要记录的想法或事件内容") String thought) {
    try {
      Long userId = UserContext.requireCurrentUserId();
      Long fudiId = getFudiId(userId);
      String conversationId = new ConversationId(ChatType.SPIRIT, userId, fudiId).value();
      chatMemory.add(conversationId, List.of(new SystemMessage(thought)));

      log.debug("地灵想法已记录 - userId: {}, thought: {}", userId, thought);
      return new AddThoughtResponse(true, "想法已记录");
    } catch (Exception e) {
      log.error("记录想法失败 - error: {}", e.getMessage());
      return new AddThoughtResponse(false, "记录想法失败：" + e.getMessage());
    }
  }

  private Long getFudiId(Long userId) {
    return fudiRepository
        .findByUserId(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.FUDI_NOT_FOUND))
        .getId();
  }

  // ===================== 响应 Record 定义 =====================

  public record UpdateEmotionResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message,
      @JsonPropertyDescription("当前情绪状态") String emotionState) {}

  public record AddThoughtResponse(
      @JsonPropertyDescription("是否成功") boolean success,
      @JsonPropertyDescription("结果消息") String message) {}
}
