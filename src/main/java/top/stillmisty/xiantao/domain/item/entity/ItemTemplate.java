package top.stillmisty.xiantao.domain.item.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.EquipmentSlot;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.domain.item.enums.Rarity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private UUID id;

    /**
     * 物品名称
     */
    private String name;

    /**
     * 物品类型
     */
    private ItemType type;

    /**
     * 装备部位（仅装备类）
     */
    private EquipmentSlot slot;

    /**
     * 基础稀有度（仅装备类，用于掉落权重计算）
     */
    private Rarity baseRarity;

    /**
     * 基础属性加成 JSONB: {"str":5,"con":3,"agi":2,"wis":0}
     */
    private Map<String, Integer> baseStats;

    /**
     * 基础攻击力（仅武器）
     */
    private Integer baseAttack;

    /**
     * 基础防御力（仅防具）
     */
    private Integer baseDefense;

    /**
     * 装备等级（仅装备类，用于计算词条数值）
     */
    private Integer equipLevel;

    /**
     * 掉落权重 JSONB（仅装备类）
     * 示例: {"BROKEN": 50, "COMMON": 30, "RARE": 15, "EPIC": 4, "LEGENDARY": 1}
     */
    private Map<String, Integer> dropWeight;

    /**
     * 物品标签 JSONB
     * 用于：材料锻造需求、NPC喜好判断、地灵AI识别
     * 示例: ["ore", "metal", "forge_base"] 或 ["seed", "fire", "rare"]
     */
    private List<String> tags;

    /**
     * 生长时间（小时，仅种子/灵蛋）
     */
    private Integer growTime;

    /**
     * 成熟后产出的物品模板ID（仅种子/灵蛋）
     */
    private String yieldId;

    /**
     * 存活率百分比（仅种子/灵蛋）
     */
    private Integer surviveRate;

    /**
     * 最大堆叠数量（仅标品）
     */
    private Integer maxStack;

    /**
     * 物品描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ===================== 业务逻辑方法 =====================

    /**
     * 检查是否为装备类
     */
    public boolean isEquipment() {
        return type != null && type == ItemType.EQUIPMENT;
    }

    /**
     * 检查是否为福地专供类
     */
    public boolean isFudiItem() {
        return type != null && type.isFudiItem();
    }

    /**
     * 检查是否为炼药类
     */
    public boolean isAlchemyItem() {
        return type != null && type.isAlchemyItem();
    }

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

    /**
     * 获取指定品质的掉落权重
     */
    public Integer getDropWeightForRarity(Rarity rarity) {
        if (dropWeight == null) return 0;
        return dropWeight.getOrDefault(rarity.getCode(), 0);
    }

    /**
     * 获取总掉落权重
     */
    public int getTotalDropWeight() {
        if (dropWeight == null) return 0;
        return dropWeight.values().stream().mapToInt(Integer::intValue).sum();
    }
}
