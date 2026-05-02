package top.stillmisty.xiantao.domain.item.enums;

import lombok.Getter;

/**
 * 物品类型枚举
 */
@Getter
public enum ItemType {

    MATERIAL("material", "材料"),

    SEED("seed", "种子"),

    BEAST_EGG("beast_egg", "灵兽卵"),

    POTION("potion", "丹药"),

    EVOLUTION_STONE("evolution_stone", "进化石"),

    SKILL_JADE("skill_jade", "法决玉简"),

    RECIPE_SCROLL("recipe_scroll", "丹方卷轴"),

    HERB("herb", "药材");

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
