package top.stillmisty.xiantao.domain.monster.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.monster.enums.MonsterType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_monster_template")
public class MonsterTemplate {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;

  @Nullable private String description;

  private MonsterType monsterType;

  private Integer baseLevel;

  private Integer baseHp;

  private Integer baseAttack;

  private Integer baseDefense;

  private Integer baseSpeed;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<Long> skills;

  private Integer expReward;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<String> tags;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<DropTableEntry> dropTable;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()")
  private LocalDateTime updateTime;
}
