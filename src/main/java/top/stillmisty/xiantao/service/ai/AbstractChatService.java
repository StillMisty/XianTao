package top.stillmisty.xiantao.service.ai;

import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import top.stillmisty.xiantao.domain.chat.enums.ChatType;
import top.stillmisty.xiantao.service.BusinessException;
import top.stillmisty.xiantao.service.ServiceResult;

/**
 * 对话 module — 一次对话的完整生命周期：频控 → 构建对话（加载/刷新/Prompt）→ 绑定上下文 → LLM 调用 → 兜底文案。
 *
 * <p>子类只负责「构建这一次对话」；工具经 {@link ChatContext#require} 读取预加载数据，不再回退查库。
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChatService {

  protected final ChatClient chatClient;
  protected final ChatMemory chatMemory;
  private final AiChatRateLimiter rateLimiter;

  /** 一次对话的输入：会话标识、Prompt、工具、可选上下文与对话后动作。 */
  protected record ChatTurn(
      ChatType chatType,
      Long userId,
      Long entityId,
      String systemPrompt,
      String userInput,
      List<Object> tools,
      @Nullable Object context,
      @Nullable Runnable afterTurn) {}

  /** 对话兜底文案：空回复 / 业务失败 / 未知异常。 */
  protected record ChatReplies(String empty, String businessFailure, String error) {}

  /**
   * 进行一次对话：频控通过后构建对话并调用 LLM，任何失败都收敛为 {@link ServiceResult}。
   *
   * @param userId 频控与日志用的玩家 ID
   * @param replies 各类失败的兜底文案
   * @param turnFactory 构建这一次对话（加载数据、对话前刷新、组装 Prompt 与上下文）
   */
  protected ServiceResult<String> converse(
      Long userId, ChatReplies replies, Supplier<ChatTurn> turnFactory) {
    rateLimiter.checkAllowed(userId);
    try {
      ChatTurn turn = turnFactory.get();
      String reply =
          turn.context() == null
              ? callLlm(turn)
              : ChatContext.with(turn.context(), () -> callLlm(turn));
      if (turn.afterTurn() != null) {
        turn.afterTurn().run();
      }
      return new ServiceResult.Success<>(
          reply != null && !reply.isBlank() ? reply : replies.empty());
    } catch (BusinessException e) {
      return ServiceResult.businessFailure(
          e.getMessage() != null ? e.getMessage() : replies.businessFailure());
    } catch (Exception e) {
      log.error("对话失败 - userId: {}, error: {}", userId, e.getMessage(), e);
      return ServiceResult.businessFailure(replies.error());
    }
  }

  @Nullable
  private String callLlm(ChatTurn turn) {
    String conversationId =
        new ConversationId(turn.chatType(), turn.userId(), turn.entityId()).value();

    ChatResponse chatResponse =
        chatClient
            .prompt()
            .system(turn.systemPrompt())
            .user(turn.userInput())
            // 历史记忆由 MessageChatMemoryAdvisor 统一加载，此处不再手动 get（避免每轮双查 chat_history）
            .tools(turn.tools().toArray())
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .chatResponse();

    if (chatResponse == null || chatResponse.getResult() == null) {
      log.warn("LLM returned null response for conversation: {}", conversationId);
      return null;
    }

    AssistantMessage output = chatResponse.getResult().getOutput();
    if (output == null) {
      log.warn("LLM returned null output for conversation: {}", conversationId);
      return null;
    }

    String content = output.getText() != null ? output.getText() : "";
    return content.isEmpty() ? null : content;
  }
}
