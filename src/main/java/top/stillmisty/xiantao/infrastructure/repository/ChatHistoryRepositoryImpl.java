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
  public List<ChatHistory> findByChatTypeAndConversationIdAndUserIdOrderByCreateTimeDesc(
      ChatType chatType, Long conversationId, Long userId, int limit) {
    QueryWrapper query =
        new QueryWrapper()
            .eq(ChatHistory::getChatType, chatType)
            .eq(ChatHistory::getConversationId, conversationId)
            .eq(ChatHistory::getUserId, userId)
            .orderBy(ChatHistory::getCreateTime, false)
            .limit(limit);
    return mapper.selectListByQuery(query);
  }

  @Override
  public void deleteOldEntries(ChatType chatType, Long conversationId, Long userId, int keepCount) {
    mapper.deleteOldEntries(chatType.getCode(), conversationId, userId, keepCount);
  }
}
