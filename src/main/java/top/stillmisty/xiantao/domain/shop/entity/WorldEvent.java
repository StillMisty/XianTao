package top.stillmisty.xiantao.domain.shop.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("world_event")
public class WorldEvent {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String title;

  private String description;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<String> affectedTags;

  private BigDecimal globalMultiplier;

  private BigDecimal affectedMin;

  private BigDecimal affectedMax;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String createdBy;

  // ===================== 业务逻辑方法 =====================

  public boolean isActive() {
    var now = LocalDateTime.now();
    return startTime != null
        && endTime != null
        && !now.isBefore(startTime)
        && !now.isAfter(endTime);
  }

  public double getGlobalMultiplierDouble() {
    return globalMultiplier != null ? globalMultiplier.doubleValue() : 1.0;
  }

  public boolean affectsTag(String tag) {
    return affectedTags != null && affectedTags.contains(tag);
  }

  public boolean affectsAnyTag(Set<String> tags) {
    if (affectedTags == null || tags == null) return false;
    return tags.stream().anyMatch(affectedTags::contains);
  }
}
