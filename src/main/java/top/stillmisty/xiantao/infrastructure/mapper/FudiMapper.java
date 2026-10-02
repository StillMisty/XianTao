package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.fudi.entity.Fudi;

/** 福地 Mapper 接口 */
@Mapper
public interface FudiMapper extends BaseMapper<Fudi> {

  /** 条件更新占用福地事件生成时点（距上次事件已满最小间隔才允许），返回受影响行数。 */
  @Update(
      "UPDATE fudi SET last_event_time = NOW() WHERE id = #{fudiId} AND (last_event_time IS NULL OR"
          + " last_event_time <= #{notBefore})")
  int tryClaimEventGeneration(
      @Param("fudiId") Long fudiId, @Param("notBefore") LocalDateTime notBefore);
}
