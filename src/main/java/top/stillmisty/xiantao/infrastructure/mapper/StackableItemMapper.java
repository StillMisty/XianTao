package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.item.entity.StackableItem;

@Mapper
public interface StackableItemMapper extends BaseMapper<StackableItem> {

  @Update(
      "UPDATE xt_inventory_item SET quantity = quantity - #{qty} WHERE id = #{id} AND user_id = #{userId} AND quantity >= #{qty}")
  int reduceQuantityById(@Param("id") Long id, @Param("userId") Long userId, @Param("qty") int qty);
}
