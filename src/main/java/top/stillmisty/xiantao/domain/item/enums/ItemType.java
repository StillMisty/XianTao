package top.stillmisty.xiantao.domain.item.enums;

import lombok.Getter;

/**
 * 物品类型枚举
 */
@Getter
public enum ItemType {

    /**
     * 装备 - 非标品，具有唯一UUID
     */
    EQUIPMENT("equipment", "装备"),

    /**
     * 材料 - 标品，可堆叠
     */
    MATERIAL("material", "材料"),

    /**
     * 种子 - 标品，可堆叠
     */
    SEED("seed", "种子"),

    /**
     * 灵蛋 - 标品，可堆叠
     */
    SPIRIT_EGG("spirit_egg", "灵蛋"),

    /**
     * 消耗品 - 标品，可堆叠
     */
    CONSUMABLE("consumable", "消耗品"),

    /**
     * 草药 - 标品，用于炼药
     */
    HERB("herb", "草药"),

    /**
     * 丹药 - 标品，消耗品，恢复或Buff
     */
    POTION("potion", "丹药"),

    /**
     * 珍礼 - 标品，送给NPC提升好感
     */
    GIFT("gift", "珍礼");

    private final String code;
    private final String name;

    ItemType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 检查是否为可堆叠物品
     */
    public boolean isStackable() {
        return this != EQUIPMENT;
    }

    /**
     * 检查是否为福地专供类（种子/灵蛋）
     */
    public boolean isFudiItem() {
        return this == SEED || this == SPIRIT_EGG;
    }

    /**
     * 检查是否为炼药类（草药/丹药）
     */
    public boolean isAlchemyItem() {
        return this == HERB || this == POTION;
    }

    /**
     * 检查是否需要标签字段
     */
    public boolean requiresTags() {
        return this == MATERIAL || this == HERB || this == POTION || this == GIFT;
    }

    public static ItemType fromCode(String code) {
        for (ItemType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
