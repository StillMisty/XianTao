package top.stillmisty.xiantao.domain.item.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

/** 物品模板配置实体 存储所有物品的静态配置数据 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_item_template")
public class ItemTemplate {

  private static final ObjectMapper OBJECT_MAPPER =
      new com.fasterxml.jackson.databind.ObjectMapper();

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 物品名称（全表唯一，可跨环境用作语义标识） */
  private String name;

  /** 物品类型 */
  private ItemType type;

  /**
   * 类型特有属性 JSONB 种子/兽卵: {"grow_time":24} 法决玉简: {"skill_id":1} 丹方卷轴:
   * {"grade":3,"product":{"item_id":1,"quantity":1},"requirements":[{"element":"metal","min":1,"max":5}]}
   * 丹药: {"effects":[{"type":"exp","amount":100}]} 灵兽: {"production_items":[...],"skill_pool":{...}}
   */
  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> properties;

  /** 物品标签 JSONB 用于：材料锻造需求、NPC喜好判断、地灵AI识别 */
  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<String> tags;

  /** 物品基准价（灵石），系统配置，用于计算收购/售价 */
  private Long baseValue;

  /** 物品描述 */
  private String description;

  /** 创建时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  // ===================== 业务逻辑方法 =====================
  /** 更新时间 */
  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  /** 检查是否包含指定标签 */
  public boolean hasTag(String tag) {
    if (tags == null || tags.isEmpty()) return false;
    return tags.stream().anyMatch(t -> t.equalsIgnoreCase(tag));
  }

  // ===================== properties 访问器 =====================

  /** 检查是否包含所有指定标签 */
  public boolean hasAllTags(List<String> requiredTags) {
    if (tags == null || tags.isEmpty()) return false;
    return tags.containsAll(requiredTags.stream().map(String::toLowerCase).toList());
  }

  /** 按 type 路由返回类型安全属性对象 */
  public ItemProperties typedProperties() {
    if (properties == null || properties.isEmpty() || type == null) return null;
    return switch (type) {
      case SEED -> OBJECT_MAPPER.convertValue(properties, ItemProperties.Growth.class);
      case BEAST_EGG -> OBJECT_MAPPER.convertValue(properties, ItemProperties.BeastEgg.class);
      case SKILL_JADE -> OBJECT_MAPPER.convertValue(properties, ItemProperties.SkillJade.class);
      case POTION -> OBJECT_MAPPER.convertValue(properties, ItemProperties.Potion.class);
      case RECIPE_SCROLL -> OBJECT_MAPPER.convertValue(properties, ItemProperties.Scroll.class);
      case FORGING_BLUEPRINT ->
          OBJECT_MAPPER.convertValue(properties, ItemProperties.ForgingBlueprint.class);
      default -> null;
    };
  }

  // ===================== 便捷访问方法 =====================

  public Integer getGrowTime() {
    var props = typedProperties();
    if (props instanceof ItemProperties.Growth g) return g.growTime();
    if (props instanceof ItemProperties.BeastEgg e) return e.growTime();
    return null;
  }
}
