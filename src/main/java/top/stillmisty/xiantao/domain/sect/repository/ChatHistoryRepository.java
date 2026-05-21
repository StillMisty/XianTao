package top.stillmisty.xiantao.domain.sect.repository;

import java.util.List;
import top.stillmisty.xiantao.domain.sect.entity.ChatHistory;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;

public interface ChatHistoryRepository {
  ChatHistory save(ChatHistory history);

  List<ChatHistory> findAllByChatTypeAndConversationIdAndUserId(
      ChatType chatType, Long conversationId, Long userId);

  void deleteByChatTypeAndConversationIdAndUserId(
      ChatType chatType, Long conversationId, Long userId);

  void deleteOldestEntries(ChatType chatType, Long conversationId, Long userId, int keepCount);
}
