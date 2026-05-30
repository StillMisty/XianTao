package top.stillmisty.xiantao.domain.dungeon.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.dungeon.enums.DungeonElementType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.AccessRulesTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.AreaConfigsTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.SpiritConfigTypeHandler;

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

  @Nullable private DungeonElementType elementType;
  private Integer minLevel;
  private Integer maxLevel;
  private Integer maxTeamSize;
  private Integer timeoutHours;
  private Boolean isActive;

  @Column(typeHandler = AccessRulesTypeHandler.class)
  @Nullable
  private List<AccessCondition> accessRules;

  @Column(typeHandler = SpiritConfigTypeHandler.class)
  @Nullable
  private SpiritConfig spiritConfig;

  @Column(typeHandler = AreaConfigsTypeHandler.class)
  private List<AreaConfig> areaConfigs;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  public boolean hasSpirit() {
    return spiritConfig != null && spiritConfig.spiritName() != null;
  }

  public boolean hasAffectionSystem() {
    return spiritConfig != null
        && spiritConfig.affectionSystem() != null
        && spiritConfig.affectionSystem();
  }

  public record AccessCondition(
      String type,
      @JsonProperty("node_ids") @Nullable List<Long> nodeIds,
      @Nullable Integer min,
      @Nullable Integer max,
      @JsonProperty("sect_id") @Nullable Long sectId,
      @JsonProperty("template_id") @Nullable Long templateId,
      @JsonProperty("dungeon_id") @Nullable Long dungeonId,
      @Nullable String code) {}

  public record SpiritConfig(
      @JsonProperty("spirit_name") @Nullable String spiritName,
      @JsonProperty("spirit_appearance") @Nullable String spiritAppearance,
      @Nullable String personality,
      @JsonProperty("tone_style") @Nullable String toneStyle,
      @Nullable String greeting,
      @JsonProperty("affection_system") @Nullable Boolean affectionSystem) {}

  public record AreaConfig(
      String key,
      String name,
      String description,
      String type,
      @JsonProperty("main_pois") List<Poi> mainPois,
      @JsonProperty("hidden_pois") List<Poi> hiddenPois,
      @JsonProperty("hidden_areas") @Nullable List<HiddenArea> hiddenAreas) {

    public boolean isMain() {
      return "MAIN".equals(type);
    }

    public List<Poi> allPois() {
      var list = new java.util.ArrayList<>(mainPois());
      if (hiddenPois() != null) list.addAll(hiddenPois());
      return list;
    }
  }

  public record Poi(
      String name,
      String type,
      String description,
      @JsonProperty("loot_pool") @Nullable List<LootEntry> lootPool,
      @JsonProperty("monster_pool") @Nullable List<MonsterEntry> monsterPool,
      @Nullable List<String> clues) {

    public boolean isCombat() {
      return "COMBAT".equals(type);
    }

    public boolean isSearch() {
      return "SEARCH".equals(type);
    }

    public boolean isGather() {
      return "GATHER".equals(type);
    }

    public boolean isPassage() {
      return "PASSAGE".equals(type);
    }

    public boolean hasLootPool() {
      return lootPool != null && !lootPool.isEmpty();
    }

    public boolean hasMonsterPool() {
      return monsterPool != null && !monsterPool.isEmpty();
    }

    public boolean hasClues() {
      return clues != null && !clues.isEmpty();
    }
  }

  public record LootEntry(
      @JsonProperty("template_id") Long templateId,
      @JsonProperty("min_qty") @Nullable Integer minQty,
      @JsonProperty("max_qty") @Nullable Integer maxQty,
      int weight) {}

  public record MonsterEntry(
      @JsonProperty("template_id") Long templateId,
      @JsonProperty("min_count") @Nullable Integer minCount,
      @JsonProperty("max_count") @Nullable Integer maxCount,
      int weight) {}

  public record HiddenArea(
      String key, @JsonProperty("trigger_after_resolve") List<String> triggerAfterResolve) {}
}
