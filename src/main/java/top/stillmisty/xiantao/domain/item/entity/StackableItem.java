package top.stillmisty.xiantao.domain.item.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 堆叠类物品实例实体
 * 用于存储：材料、种子、灵蛋、消耗品、草药、丹药、珍礼
 */
@Data
@Table("xt_inventory_item")
public class StackableItem {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 持有者用户ID
     */
    private UUID userId;

    /**
     * 物品模板ID (关联静态配置)
     */
    private UUID templateId;

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
     * 示例: ["ore", "metal", "forge_base"] 或 ["seed", "fire", "rare"]
     */
    private List<String> tags;

    /**
     * 生长时间（小时，仅种子/灵蛋）
     * 示例: 24 表示24小时成熟
     */
    private Integer growTime;

    /**
     * 成熟后产出的物品模板ID（仅种子/灵蛋）
     * 示例: "herb_fire_lotus"
     */
    private String yieldId;

    /**
     * 存活率百分比（仅种子/灵蛋）
     * 示例: 80 表示80%的存活率，受福地灵气浓郁度和玩家智慧影响
     */
    private Integer surviveRate;

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
     * 创建堆叠物品实例
     */
    public static StackableItem create(UUID userId, UUID templateId, ItemType itemType,
                                       String name, Integer quantity) {
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
     * 创建带标签的堆叠物品
     */
    public static StackableItem createWithTags(UUID userId, UUID templateId, ItemType itemType,
                                              String name, Integer quantity, List<String> tags) {
        StackableItem item = create(userId, templateId, itemType, name, quantity);
        item.tags = tags;
        return item;
    }

    /**
     * 创建福地专供物品（种子/灵蛋）
     */
    public static StackableItem createFudiItem(UUID userId, UUID templateId, ItemType itemType,
                                               String name, Integer quantity,
                                               Integer growTime, String yieldId, Integer surviveRate) {
        StackableItem item = create(userId, templateId, itemType, name, quantity);
        item.growTime = growTime;
        item.yieldId = yieldId;
        item.surviveRate = surviveRate;
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
     * @param amount 减少的数量
     * @return 是否还有剩余
     */
    public boolean reduceQuantity(int amount) {
        if (this.quantity == null || this.quantity < amount) {
            return false;
        }
        this.quantity -= amount;
        return this.quantity > 0;
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
     * 是否为福地专供物品
     */
    public boolean isFudiItem() {
        return itemType != null && itemType.isFudiItem();
    }
}
