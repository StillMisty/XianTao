package top.stillmisty.xiantao.domain.item.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 装备部位枚举
 */
@Getter
public enum EquipmentSlot {

    /**
     * 武器
     */
    WEAPON("weapon", "武器"),

    /**
     * 护甲
     */
    ARMOR("armor", "护甲"),

    /**
     * 饰品
     */
    ACCESSORY("accessory", "饰品"),

    /**
     * 头盔
     */
    HELMET("helmet", "头盔"),

    /**
     * 鞋子
     */
    BOOTS("boots", "鞋子");

    @EnumValue
    private final String code;
    private final String name;

    EquipmentSlot(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static EquipmentSlot fromCode(String code) {
        for (EquipmentSlot slot : values()) {
            if (slot.code.equalsIgnoreCase(code)) {
                return slot;
            }
        }
        return null;
    }

    public static EquipmentSlot fromChineseName(String name) {
        for (EquipmentSlot slot : values()) {
            if (slot.name.equals(name)) {
                return slot;
            }
        }
        return null;
    }
}
