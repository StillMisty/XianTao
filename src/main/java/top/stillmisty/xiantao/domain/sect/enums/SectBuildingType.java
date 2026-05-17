package top.stillmisty.xiantao.domain.sect.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 宗门建筑类型枚举 */
@Getter
public enum SectBuildingType {
  SCRIPTURE_PAVILION("SCRIPTURE_PAVILION", "藏经阁", 3000, 5),
  TRAINING_ROOM("TRAINING_ROOM", "练功房", 2000, 5),
  ALCHEMY_CHAMBER("ALCHEMY_CHAMBER", "炼丹房", 2000, 3),
  SPIRIT_VEIN("SPIRIT_VEIN", "灵脉", 5000, 3),
  FORGE_WORKSHOP("FORGE_WORKSHOP", "锻造坊", 2000, 3),
  GUARD_ARRAY("GUARD_ARRAY", "护阵", 4000, 3),
  HERB_GARDEN("HERB_GARDEN", "药园", 3000, 3);

  @EnumValue private final String code;
  private final String name;
  private final int buildCost;
  private final int maxLevel;

  SectBuildingType(String code, String name, int buildCost, int maxLevel) {
    this.code = code;
    this.name = name;
    this.buildCost = buildCost;
    this.maxLevel = maxLevel;
  }

  public static SectBuildingType fromCode(String code) {
    for (SectBuildingType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("未知的建筑类型: " + code);
  }

  public long upgradeCost() {
    return buildCost;
  }
}
