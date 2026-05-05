package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.pill.entity.PlayerBuff;

@Mapper
public interface PlayerBuffMapper extends BaseMapper<PlayerBuff> {

  @Select("SELECT * FROM xt_player_buff WHERE user_id = #{userId} AND expires_at > NOW()")
  List<PlayerBuff> selectActiveByUserId(@Param("userId") Long userId);

  @Select(
      "SELECT * FROM xt_player_buff WHERE user_id = #{userId} AND buff_type = #{buffType} AND expires_at > NOW()")
  List<PlayerBuff> selectActiveByUserIdAndType(
      @Param("userId") Long userId, @Param("buffType") String buffType);

  @Delete("DELETE FROM xt_player_buff WHERE user_id = #{userId} AND buff_type = #{buffType}")
  void deleteByUserIdAndType(@Param("userId") Long userId, @Param("buffType") String buffType);

  @Delete("DELETE FROM xt_player_buff WHERE expires_at <= NOW()")
  void deleteExpired();

  @Delete("DELETE FROM xt_player_buff WHERE user_id = #{userId} AND expires_at <= NOW()")
  void deleteExpiredByUserId(@Param("userId") Long userId);
}
