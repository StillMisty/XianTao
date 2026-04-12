package top.stillmisty.xiantao.domain.land.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 五行属性类型
 */
@Getter
public enum WuxingType {
    METAL(0, "金"),
    WOOD(1, "木"),
    WATER(2, "水"),
    FIRE(3, "火"),
    EARTH(4, "土");

    @EnumValue
    private final int code;
    private final String chineseName;

    WuxingType(int code, String chineseName) {
        this.code = code;
        this.chineseName = chineseName;
    }

    /**
     * 检查是否克制目标五行
     */
    public boolean overcomes(WuxingType target) {
        return switch (this) {
            case METAL -> target == WOOD;
            case WOOD -> target == EARTH;
            case WATER -> target == FIRE;
            case FIRE -> target == METAL;
            case EARTH -> target == WATER;
        };
    }

    /**
     * 检查是否生助目标五行
     */
    public boolean generates(WuxingType target) {
        return switch (this) {
            case METAL -> target == WATER;
            case WOOD -> target == FIRE;
            case WATER -> target == WOOD;
            case FIRE -> target == EARTH;
            case EARTH -> target == METAL;
        };
    }

    /**
     * 计算生长修正系数
     */
    public double calculateGrowthModifier(WuxingType adjacent) {
        if (adjacent == null || this == adjacent) {
            return 1.0;
        }
        if (this.overcomes(adjacent)) {
            // 被相邻克制，生长速度降低
            return 0.7;
        }
        if (adjacent.generates(this)) {
            // 相邻生助自己，生长速度提升
            return 1.3;
        }
        return 1.0;
    }

    public static WuxingType fromCode(int code) {
        for (WuxingType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown WuxingType code: " + code);
    }

    public static WuxingType fromChineseName(String name) {
        for (WuxingType type : values()) {
            if (type.chineseName.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown WuxingType name: " + name);
    }
}
