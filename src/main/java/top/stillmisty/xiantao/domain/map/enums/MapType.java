package top.stillmisty.xiantao.domain.map.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 地图类型枚举
 */
@Getter
public enum MapType {

    /**
     * 主城/安全区
     */
    SAFE_TOWN("safe_town", "主城/安全区"),

    /**
     * 挂机区
     */
    AFK_ZONE("afk_zone", "挂机区"),

    /**
     * 隐藏区
     */
    HIDDEN_ZONE("hidden_zone", "隐藏区");

    @EnumValue
    private final String code;
    private final String name;

    MapType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
