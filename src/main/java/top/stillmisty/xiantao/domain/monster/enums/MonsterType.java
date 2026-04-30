package top.stillmisty.xiantao.domain.monster.enums;

import lombok.Getter;

/**
 * 怪物类型枚举
 */
@Getter
public enum MonsterType {

    BEAST("beast", "妖兽"),
    SPIRIT("spirit", "灵体"),
    ARMORED("armored", "甲胄"),
    WILD_BEAST("wild_beast", "猛兽"),
    EVIL("evil", "邪祟"),
    FLYING("flying", "飞行"),
    HUMAN("human", "人形");

    private final String code;
    private final String name;

    MonsterType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MonsterType fromCode(String code) {
        for (MonsterType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        return null;
    }
}
