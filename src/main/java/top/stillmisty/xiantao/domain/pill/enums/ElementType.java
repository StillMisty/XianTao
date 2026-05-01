package top.stillmisty.xiantao.domain.pill.enums;

import lombok.Getter;

/**
 * 五行属性枚举
 */
@Getter
public enum ElementType {

    METAL("metal", "金"),
    WOOD("wood", "木"),
    WATER("water", "水"),
    FIRE("fire", "火"),
    EARTH("earth", "土");

    private final String code;
    private final String name;

    ElementType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ElementType fromCode(String code) {
        for (ElementType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}