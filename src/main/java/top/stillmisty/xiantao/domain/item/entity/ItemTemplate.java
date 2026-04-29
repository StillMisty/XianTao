package top.stillmisty.xiantao.domain.item.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.PgJsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物品模板配置实体
 * 存储所有物品的静态配置数据
 */
@Data
@Table("xt_item_template")
public class ItemTemplate {

    /**
     * 模板ID（唯一标识）
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 物品名称（全表唯一，可跨环境用作语义标识）
     */
    private String name;

    /**
     * 物品类型
     */
    private ItemType type;

    /**
     * 基础稀有度（仅装备类，用于掉落权重计算）
     */
    private Rarity baseRarity;

    /**
     * 类型特有属性 JSONB
     * 装备: {"slot":"WEAPON","equip_level":1,"base_attack":2,"base_defense":0,"base_stat_bonus":{"str":1},"drop_weight":{...}}
     * 种子: {"grow_time":24,"yields":["灵草"],"survive_rate":90}
     * 灵兽卵: {"grow_time":72,"yields":["火灵兽"],"survive_rate":70}
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> properties;

    /**
     * 物品标签 JSONB
     * 用于：材料锻造需求、NPC喜好判断、地灵AI识别
     */
    @Column(typeHandler = PgJsonbTypeHandler.class)
    private Set<String> tags;

    /**
     * 最大堆叠数量
     */
    private Integer maxStack;

    /**
     * 物品描述
     */
    private String description;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private LocalDateTime updateTime;

    // ===================== 业务逻辑方法 =====================

    /**
     * 检查是否包含指定标签
     */
    public boolean hasTag(String tag) {
        if (tags == null || tags.isEmpty()) return false;
        return tags.stream().anyMatch(t -> t.equalsIgnoreCase(tag));
    }

    /**
     * 检查是否包含所有指定标签
     */
    public boolean hasAllTags(List<String> requiredTags) {
        if (tags == null || tags.isEmpty()) return false;
        return tags.containsAll(requiredTags.stream().map(String::toLowerCase).toList());
    }

    // ===================== properties 访问器 =====================

    /**
     * 获取装备部位（仅装备类）
     */
    public EquipmentSlot getSlot() {
        if (properties == null) return null;
        Object val = properties.get("slot");
        return val != null ? EquipmentSlot.valueOf((String) val) : null;
    }

    /**
     * 获取装备等级（仅装备类）
     */
    public Integer getEquipLevel() {
        if (properties == null) return 0;
        Object val = properties.get("equip_level");
        return val instanceof Number n ? n.intValue() : 0;
    }

    /**
     * 获取基础攻击力（仅装备类）
     */
    public Integer getBaseAttack() {
        if (properties == null) return 0;
        Object val = properties.get("base_attack");
        return val instanceof Number n ? n.intValue() : 0;
    }

    /**
     * 获取基础防御力（仅装备类）
     */
    public Integer getBaseDefense() {
        if (properties == null) return 0;
        Object val = properties.get("base_defense");
        return val instanceof Number n ? n.intValue() : 0;
    }

    /**
     * 获取基础属性加成 JSONB（仅装备类）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getBaseStats() {
        if (properties == null) return Map.of();
        return (Map<String, Integer>) properties.getOrDefault("base_stat_bonus", Map.of());
    }

    /**
     * 获取掉落权重 JSONB（仅装备类）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getDropWeight() {
        if (properties == null) return Map.of();
        return (Map<String, Integer>) properties.getOrDefault("drop_weight", Map.of());
    }

    /**
     * 获取生长/孵化时间（小时，仅种子/灵兽卵）
     */
    public Integer getGrowTime() {
        if (properties == null) return null;
        Object val = properties.get("grow_time");
        return val instanceof Number n ? n.intValue() : null;
    }

    /**
     * 获取成熟后产出的物品名称集合（仅种子/灵兽卵）
     */
    @SuppressWarnings("unchecked")
    public Set<String> getYields() {
        if (properties == null) return Set.of();
        Object val = properties.get("yields");
        if (val instanceof List<?> list) return Set.copyOf((List<String>) list);
        return Set.of();
    }

    /**
     * 获取首个产出的物品名称
     */
    public String getYieldName() {
        return getYields().stream().findFirst().orElse(null);
    }

    /**
     * 获取存活率百分比（仅种子/灵兽卵）
     */
    public Integer getSurviveRate() {
        if (properties == null) return null;
        Object val = properties.get("survive_rate");
        return val instanceof Number n ? n.intValue() : null;
    }

    /**
     * 获取指定品质的掉落权重
     */
    public Integer getDropWeightForRarity(Rarity rarity) {
        Map<String, Integer> dw = getDropWeight();
        return dw.getOrDefault(rarity.getCode(), 0);
    }

    /**
     * 获取总掉落权重
     */
    public int getTotalDropWeight() {
        return getDropWeight().values().stream().mapToInt(Integer::intValue).sum();
    }
}
