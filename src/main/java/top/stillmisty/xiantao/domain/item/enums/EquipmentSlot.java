package top.stillmisty.xiantao.domain.item.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 装备部位枚举 */
@Getter
public enum EquipmentSlot {

  /** 法器 */
  WEAPON("WEAPON", "法器"),

  /** 护甲 */
  ARMOR("ARMOR", "护甲"),

  /** 饰品 */
  ACCESSORY("ACCESSORY", "饰品");

  @EnumValue private final String code;
  private final String name;

  EquipmentSlot(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static EquipmentSlot fromCode(String code) {
    for (EquipmentSlot slot : values()) {
      if (slot.code.equals(code)) {
        return slot;
      }
    }
    throw new IllegalArgumentException("Unknown EquipmentSlot code: " + code);
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
