package top.stillmisty.xiantao.domain.fudi.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 灵兽变异特性 */
@Getter
public enum MutationTrait {
  HIGH_YIELD("HIGH_YIELD", "高产", "产出量+30%"),
  RARE_PRODUCE("RARE_PRODUCE", "稀产", "5%概率产出稀有物品"),
  GUARDIAN("GUARDIAN", "护主", "天劫战力+50%"),
  SPIRITUAL("SPIRITUAL", "灵悟", "品质突破成功率+10%");

  @EnumValue private final String code;
  private final String chineseName;
  private final String description;

  MutationTrait(String code, String chineseName, String description) {
    this.code = code;
    this.chineseName = chineseName;
    this.description = description;
  }

  public static MutationTrait fromCode(String code) {
    for (MutationTrait t : values()) {
      if (t.code.equals(code)) {
        return t;
      }
    }
    throw new IllegalArgumentException("Unknown MutationTrait code: " + code);
  }
}
