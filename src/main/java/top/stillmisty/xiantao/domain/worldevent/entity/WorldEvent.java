package top.stillmisty.xiantao.domain.worldevent.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventCategory;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventScope;
import top.stillmisty.xiantao.domain.worldevent.enums.WorldEventStatus;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("world_event")
public class WorldEvent {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private WorldEventCategory category;

  private WorldEventScope scope;

  private Long regionMapNodeId;

  private String title;

  private String description;

  private WorldEventStatus status;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<String> affectedTags;

  private BigDecimal globalMultiplier;

  @Column(typeHandler = JsonbTypeHandler.class)
  private List<Map<String, Object>> effects;

  private Boolean participationEnabled;

  private Integer participationLimit;

  private Integer participationCount;

  @Column(typeHandler = JsonbTypeHandler.class)
  private List<Map<String, Object>> participationEffects;

  private Long parentEventId;

  private Integer chainOrder;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  private String createdBy;

  public boolean isActive() {
    var now = LocalDateTime.now();
    return status == WorldEventStatus.ACTIVE
        && startTime != null
        && endTime != null
        && !now.isBefore(startTime)
        && !now.isAfter(endTime);
  }

  public boolean isRegional() {
    return scope == WorldEventScope.REGIONAL;
  }

  public boolean isGlobal() {
    return scope == WorldEventScope.GLOBAL;
  }

  public double getGlobalMultiplierDouble() {
    return globalMultiplier != null ? globalMultiplier.doubleValue() : 1.0;
  }

  public boolean affectsAnyTag(Set<String> tags) {
    if (affectedTags == null || tags == null) return false;
    return tags.stream().anyMatch(affectedTags::contains);
  }

  public boolean hasEffects() {
    return effects != null && !effects.isEmpty();
  }

  public boolean canParticipate() {
    return Boolean.TRUE.equals(participationEnabled)
        && (participationLimit == null
            || participationLimit == 0
            || participationCount < participationLimit);
  }
}
