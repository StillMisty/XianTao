package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.stillmisty.xiantao.domain.sect.entity.ChatHistory;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

  @Delete(
      """
      DELETE FROM xt_chat_history
      WHERE chat_type = #{chatType} AND conversation_id = #{conversationId} AND user_id = #{userId}
      AND id NOT IN (
        SELECT id FROM xt_chat_history
        WHERE chat_type = #{chatType} AND conversation_id = #{conversationId} AND user_id = #{userId}
        ORDER BY create_time DESC
        LIMIT #{keepCount}
      )
      """)
  int deleteOldEntries(
      @Param("chatType") String chatType,
      @Param("conversationId") Long conversationId,
      @Param("userId") Long userId,
      @Param("keepCount") int keepCount);
}
