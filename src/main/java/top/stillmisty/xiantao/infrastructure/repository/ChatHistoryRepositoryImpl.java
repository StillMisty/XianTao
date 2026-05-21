package top.stillmisty.xiantao.infrastructure.repository;

import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.stillmisty.xiantao.domain.sect.entity.ChatHistory;
import top.stillmisty.xiantao.domain.sect.enums.ChatType;
import top.stillmisty.xiantao.domain.sect.repository.ChatHistoryRepository;
import top.stillmisty.xiantao.infrastructure.mapper.ChatHistoryMapper;

@Repository
@RequiredArgsConstructor
public class ChatHistoryRepositoryImpl implements ChatHistoryRepository {

  private final ChatHistoryMapper mapper;

  @Override
  public ChatHistory save(ChatHistory history) {
    mapper.insertOrUpdateSelective(history);
    return history;
  }

  @Override
  public List<ChatHistory> findAllByChatTypeAndConversationIdAndUserId(
      ChatType chatType, Long conversationId, Long userId) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(ChatHistory::getChatType, chatType)
            .eq(ChatHistory::getConversationId, conversationId)
            .eq(ChatHistory::getUserId, userId)
            .orderBy(ChatHistory::getCreateTime, true);
    return mapper.selectListByQuery(query);
  }

  @Override
  public void deleteByChatTypeAndConversationIdAndUserId(
      ChatType chatType, Long conversationId, Long userId) {
    mapper.deleteByCompositeKey(chatType.getCode(), conversationId, userId);
  }

  @Override
  public void deleteOldestEntries(
      ChatType chatType, Long conversationId, Long userId, int keepCount) {
    mapper.deleteOldestEntries(chatType.getCode(), conversationId, userId, keepCount);
  }
}
