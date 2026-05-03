package top.stillmisty.xiantao.domain.user.enums;

import lombok.Getter;

/**
 * 属性类型枚举
 */
@Getter
public enum AttributeType {
    STR("str", "力道"),
    CON("con", "根骨"),
    AGI("agi", "身法"),
    WIS("wis", "悟性");

    private final String code;
    private final String name;

    AttributeType(String code, String name) {
        this.code = code;
        this.name = name;
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
}