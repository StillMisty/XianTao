package top.stillmisty.xiantao.service.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;

public class PerTypeChatMemory implements ChatMemory {

  static final int SHOP_MAX = 20;
  static final int SPIRIT_MAX = 25;
  static final int SECT_MAX = 10;

  private final ChatMemoryRepository repository;
  private final Map<String, MessageWindowChatMemory> delegates = new ConcurrentHashMap<>();

  public PerTypeChatMemory(ChatMemoryRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Message> get(String conversationId) {
    return delegateFor(conversationId).get(conversationId);
  }

  @Override
  public void add(String conversationId, List<Message> messages) {
    delegateFor(conversationId).add(conversationId, messages);
  }

  @Override
  public void clear(String conversationId) {
    delegateFor(conversationId).clear(conversationId);
  }

  private MessageWindowChatMemory delegateFor(String conversationId) {
    ChatType type = ConversationId.from(conversationId).chatType();
    return delegates.computeIfAbsent(
        type.name(),
        t ->
            MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(resolveMax(type))
                .build());
  }

  private static int resolveMax(ChatType type) {
    return switch (type) {
      case SHOP -> SHOP_MAX;
      case SPIRIT -> SPIRIT_MAX;
      case SECT -> SECT_MAX;
      default -> SHOP_MAX;
    };
  }
}
