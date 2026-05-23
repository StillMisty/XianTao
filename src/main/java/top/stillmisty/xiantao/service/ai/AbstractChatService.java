package top.stillmisty.xiantao.service.ai;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChatService {

  protected final ChatClient chatClient;
  protected final ChatMemory chatMemory;

  protected String callLlm(
      String systemPrompt,
      String userInput,
      ChatType chatType,
      Long userId,
      Long entityId,
      Object... tools) {
    String conversationId = new ConversationId(chatType, userId, entityId).value();

    List<Message> history = chatMemory.get(conversationId);

    String content =
        chatClient
            .prompt()
            .system(systemPrompt)
            .messages(history)
            .user(userInput)
            .tools(tools)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();

    String reasoning =
        ReasoningPreservingChatCompletionService.CONVERSATION_REASONING.remove(conversationId);

    AssistantMessage assistantMessage;
    if (reasoning != null && !reasoning.isEmpty()) {
      assistantMessage =
          AssistantMessage.builder()
              .content(content)
              .properties(Map.of("reasoning_content", reasoning))
              .build();
    } else {
      assistantMessage = new AssistantMessage(content);
    }

    chatMemory.add(conversationId, List.of(new UserMessage(userInput), assistantMessage));

    return content;
  }
}
