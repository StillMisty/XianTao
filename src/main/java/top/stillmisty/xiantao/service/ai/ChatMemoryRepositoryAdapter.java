package top.stillmisty.xiantao.service.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.entity.ChatHistory;
import top.stillmisty.xiantao.domain.sect.enums.ChatRole;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.infrastructure.repository.ChatHistoryRepository;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class ChatMemoryRepositoryAdapter implements ChatMemoryRepository {

  private final ChatHistoryRepository chatHistoryRepository;

  @Override
  public List<String> findConversationIds() {
    return List.of();
  }

  @Override
  public List<Message> findByConversationId(String conversationId) {
    ConversationId cid = ConversationId.from(conversationId);
    List<ChatHistory> entries =
        chatHistoryRepository.findAllByChatTypeAndConversationIdAndUserId(
            cid.chatType(), cid.entityId(), cid.userId());

    List<Message> messages = new ArrayList<>();
    for (ChatHistory entry : entries) {
      if (entry.getRole() == ChatRole.TOOL) {
        continue;
      }
      messages.add(toMessage(entry));
    }
    return messages;
  }

  @Override
  @Transactional
  public void saveAll(String conversationId, List<Message> messages) {
    ConversationId cid = ConversationId.from(conversationId);

    chatHistoryRepository.deleteByChatTypeAndConversationIdAndUserId(
        cid.chatType(), cid.entityId(), cid.userId());

    String reasoning = extractReasoning(messages);

    int maxMessages = maxMessagesFor(cid.chatType());
    int skip = Math.max(0, messages.size() - maxMessages);
    for (int i = skip; i < messages.size(); i++) {
      Message message = messages.get(i);
      if (message.getMessageType() == MessageType.TOOL) {
        continue;
      }
      ChatHistory entry = new ChatHistory();
      entry.setChatType(cid.chatType());
      entry.setConversationId(cid.entityId());
      entry.setUserId(cid.userId());
      entry.setRole(toRole(message));
      entry.setContent(message.getText() != null ? message.getText() : "");

      if (message.getMessageType() == MessageType.ASSISTANT && reasoning != null) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(ChatHistory.REASONING_CONTENT_KEY, reasoning);
        entry.setExtraData(extraData);
      }

      chatHistoryRepository.save(entry);
    }
  }

  @Nullable
  private static String extractReasoning(List<Message> messages) {
    for (Message message : messages) {
      if (message instanceof DeepSeekAssistantMessage dsMsg) {
        String rc = dsMsg.getReasoningContent();
        if (rc != null && !rc.isEmpty()) {
          return rc;
        }
      }
    }
    return null;
  }

  private static int maxMessagesFor(ChatType type) {
    return switch (type) {
      case SHOP -> PerTypeChatMemory.SHOP_MAX;
      case SPIRIT -> PerTypeChatMemory.SPIRIT_MAX;
      case SECT -> PerTypeChatMemory.SECT_MAX;
      case DUNGEON -> PerTypeChatMemory.DUNGEON_MAX;
      default -> 25;
    };
  }

  @Override
  @Transactional
  public void deleteByConversationId(String conversationId) {
    ConversationId cid = ConversationId.from(conversationId);
    chatHistoryRepository.deleteByChatTypeAndConversationIdAndUserId(
        cid.chatType(), cid.entityId(), cid.userId());
  }

  private static Message toMessage(ChatHistory entry) {
    return switch (entry.getRole()) {
      case USER -> new UserMessage(entry.getContent());
      case ASSISTANT -> {
        Map<String, Object> extraData = entry.getExtraData();
        if (extraData != null && extraData.containsKey(ChatHistory.REASONING_CONTENT_KEY)) {
          Object reasoning = extraData.get(ChatHistory.REASONING_CONTENT_KEY);
          if (reasoning instanceof String s && !s.isEmpty()) {
            yield DeepSeekAssistantMessage.builder()
                .content(entry.getContent())
                .reasoningContent(s)
                .build();
          }
        }
        yield new AssistantMessage(entry.getContent());
      }
      case SYSTEM -> new SystemMessage(entry.getContent());
      default -> new SystemMessage(entry.getContent());
    };
  }

  private static ChatRole toRole(Message message) {
    return switch (message.getMessageType()) {
      case USER -> ChatRole.USER;
      case ASSISTANT -> ChatRole.ASSISTANT;
      case SYSTEM -> ChatRole.SYSTEM;
      case TOOL -> ChatRole.TOOL;
    };
  }
}
