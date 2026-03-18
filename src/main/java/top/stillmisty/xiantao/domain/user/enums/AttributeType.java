package top.stillmisty.xiantao.domain.user.enums;

import lombok.Getter;

/**
 * 属性类型枚举
 */
@Getter
public enum AttributeType {
    STR("str", "力量", "影响攻击力和锻造能力"),
    CON("con", "体质", "影响生命值和物理防御"),
    AGI("agi", "敏捷", "影响出手顺序和杀怪效率"),
    WIS("wis", "智慧", "影响掉宝率和经验加成");
    
    private final String code;
    private final String name;
    private final String description;
    
    AttributeType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据代码查找属性类型
     */
    public static AttributeType fromCode(String code) {
        for (AttributeType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 根据中文名称查找属性类型
     */
    public static AttributeType fromChineseName(String name) {
        for (AttributeType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}