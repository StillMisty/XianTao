package top.stillmisty.xiantao.service.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.stillmisty.xiantao.domain.sect.entity.ChatHistory;
import top.stillmisty.xiantao.domain.sect.enums.ChatRole;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.sect.repository.ChatHistoryRepository;

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
    String lastReasoning = null;
    for (ChatHistory entry : entries) {
      messages.add(toMessage(entry));
      if (entry.isFromAssistant() && entry.getExtraData() != null) {
        Object reasoning = entry.getExtraData().get(ChatRole.REASONING_CONTENT.getCode());
        if (reasoning instanceof String s && !s.isEmpty()) {
          lastReasoning = s;
        }
      }
    }
    if (lastReasoning != null) {
      ReasoningPreservingChatCompletionService.CONVERSATION_REASONING.put(
          conversationId, lastReasoning);
    }
    return messages;
  }

  @Override
  @Transactional
  public void saveAll(String conversationId, List<Message> messages) {
    ConversationId cid = ConversationId.from(conversationId);

    String reasoning = null;
    if (ReasoningScopeAdvisor.REASONING_HOLDER.isBound()) {
      AtomicReference<String> ref = ReasoningScopeAdvisor.REASONING_HOLDER.get();
      reasoning = ref != null ? ref.get() : null;
    }

    for (Message message : messages) {
      ChatHistory entry = new ChatHistory();
      entry.setChatType(cid.chatType());
      entry.setConversationId(cid.entityId());
      entry.setUserId(cid.userId());
      entry.setRole(toRole(message));
      entry.setContent(message.getText());

      if (message.getMessageType() == MessageType.ASSISTANT && reasoning != null) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put(ChatRole.REASONING_CONTENT.getCode(), reasoning);
        entry.setExtraData(extraData);
      }

      chatHistoryRepository.save(entry);
    }
    chatHistoryRepository.deleteOldestEntries(
        cid.chatType(), cid.entityId(), cid.userId(), maxMessagesFor(cid.chatType()));
  }

  private static int maxMessagesFor(ChatType type) {
    return switch (type) {
      case SHOP -> PerTypeChatMemory.SHOP_MAX;
      case SPIRIT -> PerTypeChatMemory.SPIRIT_MAX;
      case SECT -> PerTypeChatMemory.SECT_MAX;
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
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> extraData = entry.getExtraData();
        if (extraData != null && !extraData.isEmpty()) {
          metadata.putAll(extraData);
        }
        yield metadata.isEmpty()
            ? new AssistantMessage(entry.getContent())
            : AssistantMessage.builder().content(entry.getContent()).properties(metadata).build();
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
