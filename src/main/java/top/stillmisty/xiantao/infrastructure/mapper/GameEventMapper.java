package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.event.entity.GameEvent;

@Mapper
public interface GameEventMapper extends BaseMapper<GameEvent> {

  @Update(
      "<script>"
          + "UPDATE xt_game_event SET delivered = TRUE WHERE id IN "
          + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
          + "</script>")
  int markDeliveredByIds(@Param("ids") java.util.List<Long> ids);
}
