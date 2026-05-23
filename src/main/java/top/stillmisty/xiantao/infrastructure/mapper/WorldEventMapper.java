package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.worldevent.entity.WorldEvent;

@Mapper
public interface WorldEventMapper extends BaseMapper<WorldEvent> {

  @Select(
      "SELECT * FROM world_event WHERE status = 'ACTIVE' AND start_time <= NOW() AND end_time >= NOW() ORDER BY start_time DESC")
  List<WorldEvent> selectActiveEvents();

  @Select(
      "SELECT * FROM world_event WHERE status = 'ACTIVE' AND start_time <= NOW() AND end_time >= NOW() AND scope = #{scope} ORDER BY start_time DESC")
  List<WorldEvent> selectActiveByScope(@Param("scope") String scope);

  @Select(
      "SELECT * FROM world_event WHERE status = 'ACTIVE' AND start_time <= NOW() AND end_time >= NOW() AND scope = 'REGIONAL' AND region_map_node_id = #{mapNodeId} ORDER BY start_time DESC")
  List<WorldEvent> selectActiveByRegion(@Param("mapNodeId") Long mapNodeId);

  @Select("SELECT * FROM world_event WHERE status = 'UPCOMING' ORDER BY start_time ASC")
  List<WorldEvent> selectUpcoming();

  @Update("UPDATE world_event SET status = #{status} WHERE id = #{id}")
  int updateStatus(@Param("id") Long id, @Param("status") String status);

  @Update(
      "UPDATE world_event SET participation_count = participation_count + 1 WHERE id = #{id} AND (participation_limit IS NULL OR participation_count < participation_limit)")
  int incrementParticipationCount(@Param("id") Long id);
}
