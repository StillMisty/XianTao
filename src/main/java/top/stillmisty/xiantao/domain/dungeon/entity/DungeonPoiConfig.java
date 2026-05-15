package top.stillmisty.xiantao.domain.dungeon.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonArea;
import top.stillmisty.xiantao.domain.dungeon.enums.PoiType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("dungeon_poi_config")
public class DungeonPoiConfig {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long dungeonId;
  private DungeonArea area;
  private String name;
  private PoiType poiType;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<top.stillmisty.xiantao.domain.dungeon.vo.MonsterPoolEntry> monsterPool;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<top.stillmisty.xiantao.domain.dungeon.vo.LootPoolEntry> lootPool;

  private Boolean isPassage;
  private String unlockCondition;
  private Boolean isOneTime;
  private Integer exhaustedHours;

  public boolean hasMonsterPool() {
    return monsterPool != null && !monsterPool.isEmpty();
  }

  public boolean hasLootPool() {
    return lootPool != null && !lootPool.isEmpty();
  }

  public int getMonsterWeightTotal() {
    if (monsterPool == null) return 0;
    return monsterPool.stream()
        .mapToInt(top.stillmisty.xiantao.domain.dungeon.vo.MonsterPoolEntry::weight)
        .sum();
  }

  public int getLootWeightTotal() {
    if (lootPool == null) return 0;
    return lootPool.stream()
        .mapToInt(top.stillmisty.xiantao.domain.dungeon.vo.LootPoolEntry::weight)
        .sum();
  }
}
