package top.stillmisty.xiantao.domain.sect.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** 宗门商店商品 */
@EqualsAndHashCode
@Table("xt_sect_shop_item")
@Accessors(chain = true)
@Data
@SuppressWarnings("NullAway")
@NoArgsConstructor
public class SectShopItem {

  public static SectShopItem create() {
    return new SectShopItem();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long sectId;

  private Long itemTemplateId;

  private Integer priceContribution;

  private Integer stock;

  @Column(onInsertValue = "now()")
  private LocalDateTime lastRefresh;

  public boolean isInStock() {
    return stock == -1 || stock > 0;
  }

  public boolean deductStock(int quantity) {
    if (stock == -1) {
      return true;
    }
    if (stock < quantity) {
      return false;
    }
    this.stock = this.stock - quantity;
    return true;
  }
}
