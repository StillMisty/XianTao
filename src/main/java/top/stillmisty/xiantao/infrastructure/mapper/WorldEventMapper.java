package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.shop.entity.WorldEvent;

@Mapper
public interface WorldEventMapper extends BaseMapper<WorldEvent> {

  @Select(
      "SELECT * FROM world_event WHERE start_time <= NOW() AND end_time >= NOW() ORDER BY start_time DESC")
  List<WorldEvent> selectActiveEvents();
}
