package top.stillmisty.xiantao.service.ai;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
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
    for (ChatHistory entry : entries) {
      messages.add(toMessage(entry));
    }
    return messages;
  }

  @Override
  @Transactional
  public void saveAll(String conversationId, List<Message> messages) {
    ConversationId cid = ConversationId.from(conversationId);
    for (Message message : messages) {
      ChatHistory entry = new ChatHistory();
      entry.setChatType(cid.chatType());
      entry.setConversationId(cid.entityId());
      entry.setUserId(cid.userId());
      entry.setRole(toRole(message));
      entry.setContent(message.getText());
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
      case ASSISTANT -> new AssistantMessage(entry.getContent());
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
