package top.stillmisty.xiantao.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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
    return chatClient
        .prompt()
        .system(systemPrompt)
        .user(userInput)
        .tools(tools)
        .advisors(
            a ->
                a.param(ChatMemory.CONVERSATION_ID, conversationId)
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build()))
        .call()
        .content();
  }
}
