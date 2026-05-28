package top.stillmisty.xiantao.domain.beast.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 灵兽技能解锁条件 */
@Getter
public enum SkillUnlock {
  BIRTH("BIRTH", "出生"),
  TIER_2("TIER_2", "凝魄"),
  TIER_3("TIER_3", "化形"),
  TIER_4("TIER_4", "渡劫"),
  TIER_5("TIER_5", "归真");

  @EnumValue private final String code;
  private final String chineseName;

  SkillUnlock(String code, String chineseName) {
    this.code = code;
    this.chineseName = chineseName;
  }

  public static SkillUnlock fromCode(String code) {
    for (SkillUnlock s : values()) {
      if (s.code.equals(code)) return s;
    }
    throw new IllegalArgumentException("Unknown SkillUnlock code: " + code);
  }
}
