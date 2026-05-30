package top.stillmisty.xiantao.domain.dungeon.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("dungeon_instance")
public class DungeonInstance {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long dungeonId;
  private Long leaderId;
  @Nullable private Long teamId;
  private DungeonArea currentArea;
  private Boolean passageUnlocked;
  @Nullable private Long passagePoiId;
  private Boolean hasCoreToken;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<Long> exploredPois;

  private DungeonStatus status;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  private LocalDateTime expiresAt;
  @Nullable private LocalDateTime completedAt;

  public boolean isActive() {
    return status == DungeonStatus.ACTIVE;
  }

  public boolean isExpired() {
    return expiresAt != null && TimeUtil.now().isAfter(expiresAt);
  }

  public void addExploredPoi(Long poiConfigId) {
    if (exploredPois == null) {
      exploredPois = new ArrayList<>();
    }
    exploredPois.add(poiConfigId);
  }

  public boolean hasExploredPoi(Long poiConfigId) {
    return exploredPois != null && exploredPois.contains(poiConfigId);
  }

  public void advanceArea() {
    currentArea =
        switch (currentArea) {
          case OUTER -> DungeonArea.INNER;
          case INNER -> DungeonArea.CORE;
          case CORE -> null;
        };
    passageUnlocked = false;
    passagePoiId = null;
    exploredPois = new ArrayList<>();
  }

  public void markCompleted() {
    status = DungeonStatus.COMPLETED;
    completedAt = TimeUtil.now();
  }

  public void markFailed() {
    status = DungeonStatus.FAILED;
  }

  public void markAbandoned() {
    status = DungeonStatus.ABANDONED;
  }
}
