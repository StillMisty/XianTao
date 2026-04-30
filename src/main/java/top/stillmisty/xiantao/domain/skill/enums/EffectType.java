package top.stillmisty.xiantao.domain.skill.enums;

import lombok.Getter;

@Getter
public enum EffectType {
    DAMAGE("damage", "伤害"),
    ARMOR_BREAK("armor_break", "破甲"),
    SLOW("slow", "减速"),
    EXECUTE("execute", "斩杀"),
    LIFESTEAL("lifesteal", "吸血"),
    MULTI_HIT("multi_hit", "连击"),
    DOT("dot", "持续伤害");

    private final String code;
    private final String name;

    EffectType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static EffectType fromCode(String code) {
        for (EffectType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        return DAMAGE;
    }
}
