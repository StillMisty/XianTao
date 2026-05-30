package top.stillmisty.xiantao.domain.dungeon.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("dungeon_template")
public class DungeonTemplate {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;
  private String description;
  private Long mapNodeId;
  private Integer minLevel;
  private Integer maxLevel;
  private Integer maxTeamSize;
  private Integer timeoutHours;
  private Boolean isActive;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  public boolean isAccessibleBy(int playerLevel) {
    return isActive != null
        && isActive
        && playerLevel >= (minLevel != null ? minLevel : 0)
        && playerLevel <= (maxLevel != null ? maxLevel : Integer.MAX_VALUE);
  }
}
