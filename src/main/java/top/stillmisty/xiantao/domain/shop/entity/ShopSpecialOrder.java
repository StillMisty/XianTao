package top.stillmisty.xiantao.domain.shop.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.shop.enums.SpecialOrderStatus;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("shop_special_order")
public class ShopSpecialOrder {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long playerId;

  private Long shopNpcId;

  private Long templateId;

  private Long unitPrice;

  private Integer quantity;

  private Long deposit;

  private SpecialOrderStatus status;

  private Integer sourcingHours;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;
}
