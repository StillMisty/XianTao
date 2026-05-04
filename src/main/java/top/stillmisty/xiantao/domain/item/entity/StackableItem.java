package top.stillmisty.xiantao.domain.item.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.ItemType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 堆叠类物品实例实体
 * 用于存储：材料、种子、灵蛋、消耗品、草药、丹药
 */
@Data
@Table("xt_inventory_item")
public class StackableItem {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 持有者用户ID
     */
    private Long userId;

    /**
     * 物品模板ID (关联静态配置)
     */
    private Long templateId;

    /**
     * 物品类型
     */
    private ItemType itemType;

    /**
     * 物品名称 (从模板复制)
     */
    private String name;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 物品标签 JSONB，用于AI检索和NPC交互
     * 示例: ["ore", "metal", "forge_base"]
     */
    @Column(typeHandler = JsonbCollectionTypeHandler.class)
    private Set<String> tags;

    /**
     * 类型特有属性 JSONB
     * 丹药: {"grade": 3, "quality": "superior"}
     * 药材: {"elements": {"wood": 3, "fire": 1, "water": 2}}
     */
    @Column(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> properties;

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
     * 创建堆叠物品实例
     */
    public static StackableItem create(
            Long userId, Long templateId, ItemType itemType,
            String name, Integer quantity
    ) {
        StackableItem item = new StackableItem();
        item.userId = userId;
        item.templateId = templateId;
        item.itemType = itemType;
        item.name = name;
        item.quantity = quantity;
        item.createTime = LocalDateTime.now();
        return item;
    }

    /**
     * 增加数量
     */
    public void addQuantity(int amount) {
        if (this.quantity == null) {
            this.quantity = 0;
        }
        this.quantity += amount;
    }

    /**
     * 减少数量
     *
     * @param amount 减少的数量
     * @return 是否已用完（true=数量归零或不足，需要删除该物品）
     */
    public boolean reduceQuantity(int amount) {
        if (this.quantity == null || this.quantity < amount) {
            return true;
        }
        this.quantity -= amount;
        return this.quantity <= 0;
    }

    /**
     * 检查数量是否足够
     */
    public boolean hasEnoughQuantity(int amount) {
        return this.quantity != null && this.quantity >= amount;
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
     * 获取丹药品阶（仅丹药类）
     *
     * @return 品阶1~9，不存在返回0
     */
    public int getGrade() {
        if (properties == null) return 0;
        Object val = properties.get("grade");
        return val instanceof Number n ? n.intValue() : 0;
    }

    /**
     * 获取丹药成色（仅丹药类）
     *
     * @return 成色字符串（superior, normal, inferior），不存在返回null
     */
    public String getQuality() {
        if (properties == null) return null;
        Object val = properties.get("quality");
        return val instanceof String s ? s : null;
    }

    /**
     * 获取药材五行属性值（仅药材类）
     *
     * @param elementCode 五行属性代码（metal, wood, water, fire, earth）
     * @return 属性值，不存在返回0
     */
    public int getElementValue(String elementCode) {
        if (properties == null || elementCode == null) return 0;
        Object elementsObj = properties.get("elements");
        if (elementsObj instanceof Map<?, ?> elements) {
            Object val = elements.get(elementCode);
            return val instanceof Number n ? n.intValue() : 0;
        }
        return 0;
    }
}
