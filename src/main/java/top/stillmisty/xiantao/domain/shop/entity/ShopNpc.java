package top.stillmisty.xiantao.domain.shop.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("shop_npc")
public class ShopNpc {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;

  private Long mapNodeId;

  private String personality;

  private java.math.BigDecimal buyPriceModifier;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, java.math.BigDecimal> categoryMultiplier;

  private String systemPrompt;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  public double getBuyPriceModifierDouble() {
    return buyPriceModifier != null ? buyPriceModifier.doubleValue() : 0.50;
  }

  public double getCategoryMultiplier(String itemTypeCode) {
    if (categoryMultiplier == null || categoryMultiplier.isEmpty()) return 1.0;
    var multiplier = categoryMultiplier.get(itemTypeCode);
    return multiplier != null ? multiplier.doubleValue() : 1.0;
  }

  public double getHaggleDifficulty() {
    if (personality == null || personality.isBlank()) return 0.1;
    return switch (personality.toUpperCase()) {
      case "ESFJ" -> 0.0; // 热情好客
      case "ISFJ" -> 0.05; // 温和良善
      case "INTJ" -> 0.1; // 疏离冷静
      case "ESTP" -> 0.15; // 粗犷霸道
      default -> 0.1;
    };
  }
}
