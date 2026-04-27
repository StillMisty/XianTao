package top.stillmisty.xiantao.domain.land.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 地灵情绪状态
 */
@Getter
public enum EmotionState {
    HAPPY("happy", "😊", "灵气充足，无焦土地块"),
    CALM("calm", "😐", "正常状态"),
    ANXIOUS("anxious", "😰", "灵气低于30%"),
    FATIGUED("fatigued", "😴", "精力耗尽"),
    ANGRY("angry", "😤", "天劫失利，地块被毁"),
    EXCITED("excited", "🥳", "天劫胜利或玩家升级福地");

    @EnumValue
    private final String code;
    private final String emoji;
    private final String description;

    EmotionState(String code, String emoji, String description) {
        this.code = code;
        this.emoji = emoji;
        this.description = description;
    }

    public static EmotionState fromCode(String code) {
        for (EmotionState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown EmotionState code: " + code);
    }
}
