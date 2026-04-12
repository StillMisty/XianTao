package top.stillmisty.xiantao.domain.land.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * MBTI 人格类型
 */
@Getter
public enum MBTIPersonality {
    INTJ("INTJ", "战略家", "🧠", "理性"),
    INTP("INTP", "逻辑学家", "🔬", "理性"),
    ENTJ("ENTJ", "指挥官", "👑", "外向"),
    ENTP("ENTP", "辩论家", "💡", "外向"),
    INFJ("INFJ", "倡导者", "🌙", "内向"),
    INFP("INFP", "调停者", "🌸", "内向"),
    ENFJ("ENFJ", "主人公", "☀️", "外向"),
    ENFP("ENFP", "竞选者", "✨", "外向"),
    ISTJ("ISTJ", "物流师", "📋", "务实"),
    ISFJ("ISFJ", "守卫者", "🛡️", "务实"),
    ESTJ("ESTJ", "总经理", "⚙️", "务实"),
    ESFJ("ESFJ", "执政官", "🤝", "务实"),
    ISTP("ISTP", "鉴赏家", "🔧", "行动"),
    ISFP("ISFP", "探险家", "🎨", "行动"),
    ESTP("ESTP", "企业家", "🎯", "行动"),
    ESFP("ESFP", "表演者", "🎭", "行动");

    @EnumValue
    private final String code;
    private final String title;
    private final String emoji;
    private final String category;

    MBTIPersonality(String code, String title, String emoji, String category) {
        this.code = code;
        this.title = title;
        this.emoji = emoji;
        this.category = category;
    }

    public static MBTIPersonality fromCode(String code) {
        for (MBTIPersonality personality : values()) {
            if (personality.code.equals(code)) {
                return personality;
            }
        }
        throw new IllegalArgumentException("Unknown MBTI personality code: " + code);
    }
}
