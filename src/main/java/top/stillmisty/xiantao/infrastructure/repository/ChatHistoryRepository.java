package top.stillmisty.xiantao.infrastructure.repository;

import static top.stillmisty.xiantao.domain.sect.entity.table.ChatHistoryTableDef.CHAT_HISTORY;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.ChatHistory;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.infrastructure.mapper.ChatHistoryMapper;

@Repository
@RequiredArgsConstructor
public class ChatHistoryRepository {

  private final ChatHistoryMapper mapper;

  public ChatHistory save(ChatHistory history) {
    mapper.insertOrUpdateSelective(history);
    return history;
  }

  public List<ChatHistory> findAllByChatTypeAndConversationIdAndUserId(
      ChatType chatType, Long conversationId, Long userId) {
    QueryWrapper query =
        QueryWrapper.create()
            .where(CHAT_HISTORY.CHAT_TYPE.eq(chatType))
            .and(CHAT_HISTORY.CONVERSATION_ID.eq(conversationId))
            .and(CHAT_HISTORY.USER_ID.eq(userId))
            .orderBy(CHAT_HISTORY.CREATE_TIME.asc());
    return mapper.selectListByQuery(query);
  }

  public void deleteByChatTypeAndConversationIdAndUserId(
      ChatType chatType, Long conversationId, Long userId) {
    mapper.deleteByCompositeKey(chatType.getCode(), conversationId, userId);
  }

  public void deleteOldestEntries(
      ChatType chatType, Long conversationId, Long userId, int keepCount) {
    mapper.deleteOldestEntries(chatType.getCode(), conversationId, userId, keepCount);
  }
}
