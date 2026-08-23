package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChatService {

  protected final ChatClient chatClient;
  protected final ChatMemory chatMemory;

  @Nullable
  protected String callLlm(
      String systemPrompt,
      String userInput,
      ChatType chatType,
      Long userId,
      Long entityId,
      Object... tools) {
    String conversationId = new ConversationId(chatType, userId, entityId).value();

    ChatResponse chatResponse =
        chatClient
            .prompt()
            .system(systemPrompt)
            // 历史记忆由 MessageChatMemoryAdvisor 统一加载，此处不再手动 get（避免每轮双查 chat_history）
            .user(userInput)
            .tools(tools)
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

    String reasoning = null;
    if (output instanceof DeepSeekAssistantMessage dsMsg) {
      reasoning = dsMsg.getReasoningContent();
    }

    String content = output.getText() != null ? output.getText() : "";

    return content.isEmpty() ? null : content;
  }
}
