package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.skill.entity.PlayerSkill;

@Mapper
public interface PlayerSkillMapper extends BaseMapper<PlayerSkill> {

  /**
   * 原子条件装载：仅当已装载数低于上限时置为已装载，防止并发装载超额。
   *
   * @return 受影响行数，0 表示槽位已满或法决不存在
   */
  @Update(
      "UPDATE player_skill SET is_equipped = true WHERE id = #{id} "
          + "AND (SELECT COUNT(*) FROM player_skill "
          + "WHERE user_id = #{userId} AND is_equipped = true) < #{maxSlots}")
  int equipIfSlotAvailable(
      @Param("id") Long id, @Param("userId") Long userId, @Param("maxSlots") int maxSlots);
}
