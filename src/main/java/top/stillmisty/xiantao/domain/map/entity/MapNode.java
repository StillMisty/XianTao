package top.stillmisty.xiantao.domain.map.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.stillmisty.xiantao.domain.map.enums.MapType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

/** 地图节点实体 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_map_node")
@Data
@NoArgsConstructor
public class MapNode extends Model<MapNode> {

  public static MapNode create() {
    return new MapNode();
  }

  /** 地图 ID */
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 地图名称 */
  private String name;

  /** 地图描述 */
  private String description;

  /** 地图类型 */
  private MapType mapType;

  /** 推荐等级 */
  private Integer levelRequirement;

  /** 相邻地图 (JSONB) 格式: [{"targetId": 1, "cost": 5}] */
  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<NeighborEntry> neighbors;

  /** 历练掉落池/特产 (JSONB) 格式: [{"templateId": 1, "weight": 30}] */
  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<SpecialtyEntry> specialties;

  /** 遇怪池 (JSONB) 格式: [{"templateId": 1, "weight": 50, "min": 1, "max": 3}] */
  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<MonsterEncounterEntry> monsterEncounters;

  /** 创建时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  /** 更新时间 */
  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  // ===================== 业务逻辑方法 =====================

  /** 检查玩家等级是否满足要求 */
  public boolean isAccessibleBy(int playerLevel) {
    if (levelRequirement == null) return true;
    return playerLevel >= levelRequirement;
  }

  /** 获取到指定地图的旅行时间 */
  public Integer getTravelTimeTo(Long mapId) {
    if (neighbors == null) return null;
    return neighbors.stream()
        .filter(n -> n.targetId().equals(mapId))
        .findFirst()
        .map(NeighborEntry::cost)
        .orElse(null);
  }

  /** 检查是否与指定地图相邻 */
  public boolean isAdjacentTo(Long mapId) {
    if (neighbors == null) return false;
    return neighbors.stream().anyMatch(n -> n.targetId().equals(mapId));
  }

  /** 获取所有相邻地图 ID */
  public java.util.Set<Long> getAdjacentMapIds() {
    if (neighbors == null) return java.util.Set.of();
    return neighbors.stream()
        .map(NeighborEntry::targetId)
        .collect(java.util.stream.Collectors.toSet());
  }
}
