package top.stillmisty.xiantao.domain.skill.enums;

import lombok.Getter;

@Getter
public enum SkillType {
    ACTIVE("active", "主动"),
    PASSIVE("passive", "被动");

    private final String code;
    private final String name;

    SkillType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static SkillType fromCode(String code) {
        for (SkillType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        return ACTIVE;
    }
}
