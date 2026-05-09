package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.fudi.entity.Spirit;

@Mapper
public interface SpiritMapper extends BaseMapper<Spirit> {

  @Update(
      "UPDATE xt_spirit SET last_gift_time = NOW() WHERE id = #{id} AND (last_gift_time IS NULL OR last_gift_time::date < CURRENT_DATE)")
  int tryClaimDailyGift(@Param("id") Long id);
}
