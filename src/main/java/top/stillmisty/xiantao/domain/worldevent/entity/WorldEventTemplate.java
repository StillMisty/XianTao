package top.stillmisty.xiantao.domain.worldevent.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("world_event_template")
public class WorldEventTemplate {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private WorldEventCategory category;

  private WorldEventScope scope;

  private String title;

  private String description;

  private Integer cooldownHours;

  private Integer selectionWeight;

  private Integer durationHours;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<String> affectedTags;

  private java.math.BigDecimal globalMultiplier;

  @Column(typeHandler = JsonbTypeHandler.class)
  private List<Map<String, Object>> effects;

  private Boolean participationEnabled;

  private Integer participationLimit;

  @Column(typeHandler = JsonbTypeHandler.class)
  private List<Map<String, Object>> participationEffects;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<String> validRegionTags;

  private Long chainedTemplateId;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  private String createdBy;

  public double getGlobalMultiplierDouble() {
    return globalMultiplier != null ? globalMultiplier.doubleValue() : 1.0;
  }

  public int getCooldownHours() {
    return cooldownHours != null ? cooldownHours : 24;
  }

  public int getDurationHours() {
    return durationHours != null ? durationHours : 6;
  }

  public int getSelectionWeightInt() {
    return selectionWeight != null ? selectionWeight : 100;
  }
}
