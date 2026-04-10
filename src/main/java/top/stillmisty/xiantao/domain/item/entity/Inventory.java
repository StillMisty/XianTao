package top.stillmisty.xiantao.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.stillmisty.xiantao.domain.item.enums.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 背包
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    /**
     * 背包物品列表
     */
    private List<InventoryItem> items;

    /**
     * 背包最大容量
     */
    private Integer capacity;

    /**
     * 初始化背包
     */
    public static Inventory init() {
        Inventory inventory = new Inventory();
        inventory.items = new ArrayList<>();
        inventory.capacity = 50;
        return inventory;
    }

    /**
     * 获取当前物品数量
     */
    public int getCurrentSize() {
        return items != null ? items.size() : 0;
    }

    /**
     * 检查背包是否已满
     */
    public boolean isFull() {
        return getCurrentSize() >= capacity;
    }

    /**
     * 添加物品
     */
    public boolean addItem(InventoryItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        // 堆叠物品合并逻辑
        if (item.getItemType() != ItemType.EQUIPMENT) {
            for (InventoryItem existing : items) {
                if (existing.getItemId().equals(item.getItemId())) {
                    existing.setQuantity(existing.getQuantity() + item.getQuantity());
                    return true;
                }
            }
        }

        // 检查容量
        if (isFull()) {
            return false;
        }

        items.add(item);
        return true;
    }

    /**
     * 移除物品
     */
    public InventoryItem removeItem(String itemId) {
        if (items == null) return null;

        for (int i = 0; i < items.size(); i++) {
            InventoryItem item = items.get(i);
            if (item.getItemId().equals(itemId)) {
                items.remove(i);
                return item;
            }
        }
        return null;
    }

    /**
     * 减少堆叠物品数量
     */
    public int decreaseStackableItem(String itemId, int amount) {
        if (items == null) return 0;

        for (int i = 0; i < items.size(); i++) {
            InventoryItem item = items.get(i);
            if (item.getItemId().equals(itemId) && item.getItemType() != ItemType.EQUIPMENT) {
                int actualDecrease = Math.min(item.getQuantity(), amount);
                item.setQuantity(item.getQuantity() - actualDecrease);

                if (item.getQuantity() <= 0) {
                    items.remove(i);
                }
                return actualDecrease;
            }
        }
        return 0;
    }

    /**
     * 查找物品
     */
    public InventoryItem findItem(String itemId) {
        if (items == null) return null;

        return items.stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 按类型获取物品
     */
    public List<InventoryItem> getItemsByType(ItemType type) {
        if (items == null) return new ArrayList<>();

        return items.stream()
                .filter(item -> item.getItemType() == type)
                .collect(Collectors.toList());
    }

    /**
     * 按名称模糊查找物品
     */
    public List<InventoryItem> findItemsByName(String name) {
        if (items == null) return new ArrayList<>();

        return items.stream()
                .filter(item -> item.getName().contains(name))
                .collect(Collectors.toList());
    }
}
