package top.stillmisty.xiantao.infrastructure.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.stillmisty.xiantao.domain.shop.entity.ShopProduct;

@Mapper
public interface ShopProductMapper extends BaseMapper<ShopProduct> {

  @Update(
      "UPDATE shop_product SET current_stock = current_stock - #{qty} WHERE id = #{id} AND current_stock >= #{qty}")
  int deductStockIfAvailable(@Param("id") Long id, @Param("qty") int qty);

  /** 懒补货/调价结果落库（仅更新库存与价格相关列，避免全量覆盖并发修改的其他字段） */
  @Update(
      "UPDATE shop_product SET current_stock = #{currentStock}, current_price = #{currentPrice}, "
          + "last_sale_time = #{lastSaleTime}, version = version + 1 WHERE id = #{id}")
  int updateStockAndPrice(
      @Param("id") Long id,
      @Param("currentStock") int currentStock,
      @Param("currentPrice") long currentPrice,
      @Param("lastSaleTime") java.time.LocalDateTime lastSaleTime);
}
