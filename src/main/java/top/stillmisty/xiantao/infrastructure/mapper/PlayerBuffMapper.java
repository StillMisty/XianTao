package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;

@Mapper
public interface PlayerBuffMapper extends BaseMapper<PlayerBuff> {

  @Select("SELECT * FROM player_buff WHERE user_id = #{userId} AND expires_at > NOW()")
  List<PlayerBuff> selectActiveByUserId(@Param("userId") Long userId);

  @Select(
      "SELECT * FROM player_buff WHERE user_id = #{userId} AND buff_type = #{buffType} AND expires_at > NOW()")
  List<PlayerBuff> selectActiveByUserIdAndType(
      @Param("userId") Long userId, @Param("buffType") String buffType);

  @Select(
      "SELECT COUNT(*) FROM player_buff WHERE user_id = #{userId} AND buff_type = #{buffType} AND expires_at > NOW()")
  int countActiveByUserIdAndType(@Param("userId") Long userId, @Param("buffType") String buffType);

  /**
   * 原子条件插入：仅当同类活跃 buff 数低于上限时写入，防止并发服丹超层。
   *
   * @return 受影响行数，0 表示已达堆叠上限
   */
  @Insert(
      "INSERT INTO player_buff (user_id, buff_type, value, expires_at) "
          + "SELECT #{userId}, #{buffType}, #{value}, #{expiresAt} "
          + "WHERE (SELECT COUNT(*) FROM player_buff "
          + "WHERE user_id = #{userId} AND buff_type = #{buffType} AND expires_at > NOW()) < #{maxStack}")
  int insertIfBelowStackLimit(
      @Param("userId") Long userId,
      @Param("buffType") String buffType,
      @Param("value") int value,
      @Param("expiresAt") LocalDateTime expiresAt,
      @Param("maxStack") int maxStack);

  @Delete("DELETE FROM player_buff WHERE user_id = #{userId} AND buff_type = #{buffType}")
  void deleteByUserIdAndType(@Param("userId") Long userId, @Param("buffType") String buffType);

  @Delete("DELETE FROM player_buff WHERE expires_at <= NOW()")
  void deleteExpired();

  @Delete("DELETE FROM player_buff WHERE user_id = #{userId} AND expires_at <= NOW()")
  void deleteExpiredByUserId(@Param("userId") Long userId);
}
