package top.stillmisty.xiantao.domain.dungeon.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("dungeon_first_clear")
public class DungeonFirstClear {

  @EqualsAndHashCode.Include @Id private Long dungeonId;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<Long> teamMembers;

  @Column(onInsertValue = "now()")
  private LocalDateTime clearTime;

  private Integer durationMinutes;
}
