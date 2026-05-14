package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;

@Mapper
public interface ShopProductMapper extends BaseMapper<ShopProduct> {

  @Update(
      "UPDATE xt_shop_product SET current_stock = current_stock - #{qty} WHERE id = #{id} AND current_stock >= #{qty}")
  int deductStockIfAvailable(@Param("id") Long id, @Param("qty") int qty);
}
