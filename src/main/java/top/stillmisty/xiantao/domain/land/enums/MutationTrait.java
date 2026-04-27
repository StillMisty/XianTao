package top.stillmisty.xiantao.domain.land.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 灵兽变异特性
 */
@Getter
public enum MutationTrait {
    HIGH_YIELD("high_yield", "高产", "产出量+30%"),
    LONGEVITY("longevity", "长寿", "寿命延长50%"),
    ENERGY_SAVING("energy_saving", "节能", "灵气消耗降低20%"),
    RARE_PRODUCE("rare_produce", "稀产", "5%概率产出稀有物品"),
    GUARDIAN("guardian", "护主", "天劫战力+50%"),
    SPIRITUAL("spiritual", "灵悟", "品质突破成功率+10%");

    @EnumValue
    private final String code;
    private final String chineseName;
    private final String description;

    MutationTrait(String code, String chineseName, String description) {
        this.code = code;
        this.chineseName = chineseName;
        this.description = description;
    }

    public static MutationTrait fromCode(String code) {
        for (MutationTrait t : values()) {
            if (t.code.equals(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown MutationTrait code: " + code);
    }
}
