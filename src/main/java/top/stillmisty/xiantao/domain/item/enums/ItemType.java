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
     * 灵兽卵 - 标品，用于孵化灵兽
     */
    BEAST_EGG("beast_egg", "灵兽卵"),

    /**
     * 丹药 - 标品，消耗品，恢复或Buff
     */
    POTION("potion", "丹药"),

    /**
     * 进化石 - 标品，用于灵兽进化与品质突破
     */
    EVOLUTION_STONE("evolution_stone", "进化石"),

    /**
     * 法决玉简 - 标品，学习法决
     */
    SKILL_JADE("skill_jade", "法决玉简");

    private final String code;
    private final String name;

    ItemType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ItemType fromCode(String code) {
        for (ItemType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查是否为福地专供类（种子/灵兽卵/进化石）
     */
    public boolean isFudiItem() {
        return this == SEED || this == BEAST_EGG || this == EVOLUTION_STONE;
    }

}
