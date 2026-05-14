package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.stillmisty.xiantao.domain.shop.entity.ShopNpc;

@Mapper
public interface ShopNpcMapper extends BaseMapper<ShopNpc> {

  @Select("SELECT * FROM shop_npc WHERE map_node_id = #{mapNodeId}")
  ShopNpc selectByMapNodeId(@Param("mapNodeId") Long mapNodeId);
}
