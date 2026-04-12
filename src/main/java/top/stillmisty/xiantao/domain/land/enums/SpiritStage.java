package top.stillmisty.xiantao.domain.land.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 地灵形态阶段
 */
@Getter
public enum SpiritStage {
    STAGE_1(1, "初创之灵", "基础对话、自动收割"),
    STAGE_2(2, "底蕴之灵", "智能提升、自动喂兽、地灵建议"),
    STAGE_3(3, "化形之灵", "专属技能、地灵共鸣、羁绊对话");

    @EnumValue
    private final int code;
    private final String name;
    private final String features;

    SpiritStage(int code, String name, String features) {
        this.code = code;
        this.name = name;
        this.features = features;
    }

    public static SpiritStage fromCode(int code) {
        for (SpiritStage stage : values()) {
            if (stage.code == code) {
                return stage;
            }
        }
        throw new IllegalArgumentException("Unknown SpiritStage code: " + code);
    }
}
