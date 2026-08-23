package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.dungeon.entity.DungeonProgress;

@Mapper
public interface DungeonProgressMapper extends BaseMapper<DungeonProgress> {

  /** 原子累加对话互动次数，避免读-改-写竞态 */
  @Update(
      "UPDATE dungeon_progress SET interaction_count = COALESCE(interaction_count, 0) + 1 "
          + "WHERE user_id = #{userId} AND dungeon_id = #{dungeonId}")
  int incrementInteractionCount(@Param("userId") Long userId, @Param("dungeonId") Long dungeonId);
}
