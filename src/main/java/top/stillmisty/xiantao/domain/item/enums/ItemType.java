package top.stillmisty.xiantao.domain.item.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 物品类型枚举 */
@Getter
public enum ItemType {
  MATERIAL("MATERIAL", "材料"),

  SEED("SEED", "种子"),

  BEAST_EGG("BEAST_EGG", "兽卵"),

  POTION("POTION", "丹药"),

  EVOLUTION_STONE("EVOLUTION_STONE", "进化石"),

  SKILL_JADE("SKILL_JADE", "法决玉简"),

  RECIPE_SCROLL("RECIPE_SCROLL", "丹方卷轴"),

  HERB("HERB", "药材");

  @EnumValue private final String code;

  private final String name;

  ItemType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static ItemType fromCode(String code) {
    for (ItemType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ItemType code: " + code);
  }
}
