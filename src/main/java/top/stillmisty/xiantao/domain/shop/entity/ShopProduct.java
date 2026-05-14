package top.stillmisty.xiantao.domain.shop.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.shop.enums.ProductType;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("shop_product")
public class ShopProduct {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long shopNpcId;

  private ProductType productType;

  private Long templateId;

  private Long basePrice;

  private Long minPrice;

  private Long maxPrice;

  private Integer minStock;

  private Integer maxStock;

  private Long currentPrice;

  private Integer currentStock;

  private LocalDateTime lastSaleTime;

  private Integer version;
}
