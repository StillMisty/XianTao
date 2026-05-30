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
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonStatus;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.ExploredPoisTypeHandler;
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
  private String currentAreaKey;
  private Boolean passageUnlocked;

  @Column(typeHandler = ExploredPoisTypeHandler.class)
  private List<ExploredPoiRecord> exploredPois;

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

  public boolean hasExploredPoi(String poiName) {
    return exploredPois != null && exploredPois.stream().anyMatch(p -> p.poiName().equals(poiName));
  }

  public void addExploredPoi(String poiName) {
    if (exploredPois == null) {
      exploredPois = new ArrayList<>();
    }
    exploredPois.add(new ExploredPoiRecord(poiName));
  }

  public int exploredCount() {
    return exploredPois != null ? exploredPois.size() : 0;
  }

  public void advanceArea(String nextAreaKey) {
    currentAreaKey = nextAreaKey;
    passageUnlocked = false;
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

  public record ExploredPoiRecord(String poiName) {}
}
