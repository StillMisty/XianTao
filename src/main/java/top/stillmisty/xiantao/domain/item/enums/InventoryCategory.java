package top.stillmisty.xiantao.domain.item.enums;

import lombok.Getter;

/** 背包查询类别（供 LLM Function Calling 使用） */
@Getter
public enum InventoryCategory {
  SEED("种子"),
  EQUIPMENT("装备"),
  BEAST_EGG("兽卵"),
  MATERIAL("锻材"),
  POTION("丹药"),
  HERB("药材"),
  SKILL_JADE("法决玉简"),
  RECIPE_SCROLL("丹方卷轴"),
  FORGING_BLUEPRINT("锻造图纸"),
  BEAST_ESSENCE("灵兽精华");

  private final String chineseName;

  InventoryCategory(String chineseName) {
    this.chineseName = chineseName;
  }

  public ItemType toItemType() {
    return switch (this) {
      case SEED -> ItemType.SEED;
      case BEAST_EGG -> ItemType.BEAST_EGG;
      case MATERIAL -> ItemType.MATERIAL;
      case POTION -> ItemType.POTION;
      case HERB -> ItemType.HERB;
      case SKILL_JADE -> ItemType.SKILL_JADE;
      case RECIPE_SCROLL -> ItemType.RECIPE_SCROLL;
      case FORGING_BLUEPRINT -> ItemType.FORGING_BLUEPRINT;
      case BEAST_ESSENCE -> ItemType.BEAST_ESSENCE;
      case EQUIPMENT -> null;
    };
  }
}
