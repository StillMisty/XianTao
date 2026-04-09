package top.stillmisty.xiantao.domain.item.enums;

import lombok.Getter;

/**
 * 装备随机词条类型枚举
 */
@Getter
public enum AffixType {

    /**
     * 蛮力 - 力量 (STR) + X
     */
    STRENGTH("strength", "蛮力", "STR", "力量"),

    /**
     * 坚韧 - 体质 (CON) + X
     */
    CONSTITUTION("constitution", "坚韧", "CON", "体质"),

    /**
     * 轻灵 - 敏捷 (AGI) + X
     */
    AGILITY("agility", "轻灵", "AGI", "敏捷"),

    /**
     * 启迪 - 智慧 (WIS) + X
     */
    WISDOM("wisdom", "启迪", "WIS", "智慧"),

    /**
     * 吸血 - 伤害的5%转化为HP（仅金装）
     */
    LIFE_STEAL("life_steal", "吸血", null, "吸血"),

    /**
     * 寻宝 - 挂机极品掉率+5%（仅金装）
     */
    TREASURE_HUNT("treasure_hunt", "寻宝", null, "寻宝");

    private final String code;
    private final String name;

    /**
     * 对应的属性字段（null表示特殊词条）
     */
    private final String statField;

    /**
     * 中文名称（用于显示）
     */
    private final String displayName;

    AffixType(String code, String name, String statField, String displayName) {
        this.code = code;
        this.name = name;
        this.statField = statField;
        this.displayName = displayName;
    }

    /**
     * 检查是否为特殊词条（仅金装）
     */
    public boolean isSpecial() {
        return this == LIFE_STEAL || this == TREASURE_HUNT;
    }

    /**
     * 检查是否为属性词条（可随机生成）
     */
    public boolean isAttributeAffix() {
        return statField != null;
    }

    /**
     * 从代码获取词条类型
     */
    public static AffixType fromCode(String code) {
        for (AffixType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取所有属性词条（不含特殊词条）
     */
    public static AffixType[] getAttributeAffixes() {
        return new AffixType[]{STRENGTH, CONSTITUTION, AGILITY, WISDOM};
    }

    /**
     * 获取所有特殊词条
     */
    public static AffixType[] getSpecialAffixes() {
        return new AffixType[]{LIFE_STEAL, TREASURE_HUNT};
    }
}
