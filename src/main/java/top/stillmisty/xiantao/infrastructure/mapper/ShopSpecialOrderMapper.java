package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.shop.entity.ShopSpecialOrder;

@Mapper
public interface ShopSpecialOrderMapper extends BaseMapper<ShopSpecialOrder> {

  /** 条件更新订单状态（仅当前状态匹配时生效），用于取货/取消的并发保护 */
  @Update(
      "UPDATE shop_special_order SET status = #{toStatus} WHERE id = #{id} AND player_id ="
          + " #{playerId} AND status = #{fromStatus}")
  int updateStatusIf(
      @Param("id") Long id,
      @Param("playerId") Long playerId,
      @Param("fromStatus") String fromStatus,
      @Param("toStatus") String toStatus);
}
