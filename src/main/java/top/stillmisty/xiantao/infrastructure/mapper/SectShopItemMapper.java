package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.sect.entity.SectShopItem;

@Mapper
public interface SectShopItemMapper extends BaseMapper<SectShopItem> {

  /** 原子扣减库存（stock=-1 表示无限囤货，不递减）。库存不足返回 0。 */
  @Update(
      "UPDATE sect_shop_item SET stock = CASE WHEN stock = -1 THEN -1 ELSE stock - 1 END "
          + "WHERE id = #{id} AND (stock = -1 OR stock > 0)")
  int deductStockIfAvailable(@Param("id") Long id);
}
