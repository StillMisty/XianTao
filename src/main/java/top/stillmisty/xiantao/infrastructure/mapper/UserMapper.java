package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.user.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

  @Update(
      "UPDATE xt_user SET spirit_stones = spirit_stones - #{cost} WHERE id = #{userId} AND spirit_stones >= #{cost}")
  int deductSpiritStonesIfEnough(@Param("userId") Long userId, @Param("cost") long cost);

  @Update("UPDATE xt_user SET spirit_stones = spirit_stones + #{amount} WHERE id = #{userId}")
  int addSpiritStonesAtomically(@Param("userId") Long userId, @Param("amount") long amount);

  @Update(
      """
      UPDATE xt_user SET status = 'IDLE', activity_type = NULL,
      activity_start_time = NULL, activity_target_id = NULL, update_time = now()
      WHERE id = #{userId}""")
  void clearActivity(@Param("userId") Long userId);

  @Update(
      """
      UPDATE xt_user SET status = #{status}, activity_type = #{activityType},
      activity_start_time = #{activityStartTime}, activity_target_id = #{activityTargetId},
      update_time = now()
      WHERE id = #{userId}""")
  void startActivity(
      @Param("userId") Long userId,
      @Param("status") String status,
      @Param("activityType") String activityType,
      @Param("activityStartTime") LocalDateTime activityStartTime,
      @Param("activityTargetId") Long activityTargetId);

  @Update(
      """
      UPDATE xt_user SET hp_current = #{hpCurrent}, status = #{status},
      dying_start_time = #{dyingStartTime}, update_time = now()
      WHERE id = #{userId}""")
  void updateHpStatus(
      @Param("userId") Long userId,
      @Param("hpCurrent") int hpCurrent,
      @Param("status") String status,
      @Param("dyingStartTime") LocalDateTime dyingStartTime);

  @Update(
      """
      UPDATE xt_user SET hp_current = #{hpCurrent}, exp = #{exp},
      status = #{status}, dying_start_time = #{dyingStartTime},
      activity_type = #{activityType}, activity_start_time = #{activityStartTime},
      activity_target_id = #{activityTargetId}, update_time = now()
      WHERE id = #{userId}""")
  void completeTraining(
      @Param("userId") Long userId,
      @Param("hpCurrent") int hpCurrent,
      @Param("exp") long exp,
      @Param("status") String status,
      @Param("dyingStartTime") LocalDateTime dyingStartTime,
      @Param("activityType") String activityType,
      @Param("activityStartTime") LocalDateTime activityStartTime,
      @Param("activityTargetId") Long activityTargetId);

  @Update(
      """
      UPDATE xt_user SET hp_current = #{hpCurrent}, exp = #{exp},
      last_settlement_minute = #{lastSettlementMinute}, update_time = now()
      WHERE id = #{userId}""")
  void updateTrainingSettlement(
      @Param("userId") Long userId,
      @Param("hpCurrent") int hpCurrent,
      @Param("exp") long exp,
      @Param("lastSettlementMinute") long lastSettlementMinute);

  @Select("SELECT * FROM xt_user WHERE id = #{id} FOR UPDATE")
  User selectByIdForUpdate(@Param("id") Long id);
}
