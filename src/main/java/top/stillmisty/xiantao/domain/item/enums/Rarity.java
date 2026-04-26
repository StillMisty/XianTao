package top.stillmisty.xiantao.domain.item.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

import java.util.List;

/**
 * 物品稀有度/品质枚举
 * 用于装备波动系统
 */
@Getter
public enum Rarity {

    /**
     * 破旧 (白色)
     * 系数范围: 0.8 - 0.99, 附加词条: 0 条
     * 前缀示例: 破烂的、生锈的
     */
    BROKEN(
            "broken", "破旧", RarityColor.WHITE, 0.8, 0.99, 0, 0,
            List.of("破烂的", "生锈的", "残破的", "陈旧的")
    ),

    /**
     * 普通 (绿色)
     * 系数范围: 1.0 - 1.15, 附加词条: 1 条
     * 前缀示例: 普通的、结实的
     */
    COMMON(
            "common", "普通", RarityColor.GREEN, 1.0, 1.15, 1, 1,
            List.of("普通的", "结实的", "标准的", "常规的")
    ),

    /**
     * 稀有 (蓝色)
     * 系数范围: 1.16 - 1.35, 附加词条: 1-2 条
     * 前缀示例: 锋利的、精工的
     */
    RARE(
            "rare", "稀有", RarityColor.BLUE, 1.16, 1.35, 1, 2,
            List.of("锋利的", "精工的", "锐利的", "精制的")
    ),

    /**
     * 史诗 (紫色)
     * 系数范围: 1.36 - 1.60, 附加词条: 2-3 条
     * 前缀示例: 卓越的、附魔的
     */
    EPIC(
            "epic", "史诗", RarityColor.PURPLE, 1.36, 1.60, 2, 3,
            List.of("卓越的", "附魔的", "辉煌的", "不凡的")
    ),

    /**
     * 传说 (金色)
     * 系数范围: 1.61 - 2.00, 附加词条: 3-4 条
     * 前缀示例: 传说的、完美的
     */
    LEGENDARY(
            "legendary", "传说", RarityColor.GOLD, 1.61, 2.00, 3, 4,
            List.of("传说的", "完美的", "神话的", "神级的")
    );

    @EnumValue
    private final String code;
    private final String name;

    /**
     * 品质颜色
     */
    private final RarityColor color;

    /**
     * 品质系数范围 (最小值)
     */
    private final double qualityMultiplierMin;

    /**
     * 品质系数范围 (最大值)
     */
    private final double qualityMultiplierMax;

    /**
     * 最少附加词条数
     */
    private final int affixCountMin;

    /**
     * 最多附加词条数
     */
    private final int affixCountMax;

    /**
     * 品质前缀列表
     */
    private final List<String> prefixes;

    Rarity(
            String code, String name, RarityColor color, double qualityMultiplierMin, double qualityMultiplierMax,
            int affixCountMin, int affixCountMax, List<String> prefixes
    ) {
        this.code = code;
        this.name = name;
        this.color = color;
        this.qualityMultiplierMin = qualityMultiplierMin;
        this.qualityMultiplierMax = qualityMultiplierMax;
        this.affixCountMin = affixCountMin;
        this.affixCountMax = affixCountMax;
        this.prefixes = prefixes;
    }

    /**
     * 从代码获取稀有度
     */
    public static Rarity fromCode(String code) {
        for (Rarity rarity : values()) {
            if (rarity.code.equalsIgnoreCase(code)) {
                return rarity;
            }
        }
        return COMMON;
    }

    /**
     * 品质颜色枚举
     */
    @Getter
    public enum RarityColor {
        WHITE("white", "⚪", 16777215),   // 白色
        GREEN("green", "🟢", 65280),     // 绿色
        BLUE("blue", "🔵", 255),         // 蓝色
        PURPLE("purple", "🟣", 16711808), // 紫色
        GOLD("gold", "🟡", 16776960);    // 金色

        private final String code;
        private final String emoji;
        private final int colorCode;

        RarityColor(String code, String emoji, int colorCode) {
            this.code = code;
            this.emoji = emoji;
            this.colorCode = colorCode;
        }
    }
}
