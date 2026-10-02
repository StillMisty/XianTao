package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.sect.entity.Sect;

@Mapper
public interface SectMapper extends BaseMapper<Sect> {

  /** 原子增加宗门资金 */
  @Update("UPDATE sect SET funds = funds + #{amount} WHERE id = #{id}")
  int addFunds(@Param("id") Long id, @Param("amount") long amount);

  /** 原子扣减宗门资金，资金不足返回 0 */
  @Update("UPDATE sect SET funds = funds - #{amount} WHERE id = #{id} AND funds >= #{amount}")
  int deductFundsIfEnough(@Param("id") Long id, @Param("amount") long amount);

  /** 仅当距上次事件时间超过 staleBefore 时写入新事件；并发触发下只有一个请求能成功 */
  @Update(
      """
      UPDATE sect
      SET last_event_type = #{eventType},
          last_event_text = #{eventText},
          last_event_time = #{eventTime},
          event_expires_at = #{expiresAt}
      WHERE id = #{id}
        AND (last_event_time IS NULL OR last_event_time <= #{staleBefore})
      """)
  int updateEventIfStale(
      @Param("id") Long id,
      @Param("eventType") String eventType,
      @Param("eventText") String eventText,
      @Param("eventTime") LocalDateTime eventTime,
      @Param("expiresAt") LocalDateTime expiresAt,
      @Param("staleBefore") LocalDateTime staleBefore);
}
