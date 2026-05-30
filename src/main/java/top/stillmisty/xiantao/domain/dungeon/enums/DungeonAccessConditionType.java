package top.stillmisty.xiantao.domain.dungeon.enums;

import lombok.Getter;

@Getter
public enum DungeonAccessConditionType {
  MAP_NODE("MAP_NODE", "地图节点"),
  LEVEL("LEVEL", "境界"),
  SECT("SECT", "宗门"),
  ITEM("ITEM", "物品"),
  DUNGEON_CLEARED("DUNGEON_CLEARED", "通关秘境"),
  HIDDEN_COMPLETION("HIDDEN_COMPLETION", "隐藏事件");

  private final String code;
  private final String name;

  DungeonAccessConditionType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static DungeonAccessConditionType fromCode(String code) {
    for (DungeonAccessConditionType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown DungeonAccessConditionType code: " + code);
  }
}
