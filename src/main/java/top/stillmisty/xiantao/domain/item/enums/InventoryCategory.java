package top.stillmisty.xiantao.domain.item.enums;

import lombok.Getter;

/** 背包查询类别（供 LLM Function Calling 使用） */
@Getter
public enum InventoryCategory {
  SEED("种子"),
  EQUIPMENT("装备"),
  BEAST_EGG("兽卵");

  private final String chineseName;

  InventoryCategory(String chineseName) {
    this.chineseName = chineseName;
  }
}
