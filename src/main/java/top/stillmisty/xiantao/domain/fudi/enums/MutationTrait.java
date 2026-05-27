package top.stillmisty.xiantao.domain.fudi.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 灵兽变异特性 — 硬核修仙世界观 */
@Getter
public enum MutationTrait {
  // —— 攻击系 ——
  SHARP_FANG("SHARP_FANG", "锐齿", "攻击力+15%"),
  MALEVOLENCE("MALEVOLENCE", "煞气", "攻击力+25%"),

  // —— 防御系 ——
  THICK_SKIN("THICK_SKIN", "厚皮", "防御力+15%"),
  MYSTIC_ARMOR("MYSTIC_ARMOR", "玄甲", "防御力+25%"),

  // —— 速度系 ——
  SWIFT("SWIFT", "疾走", "速度+15%"),
  LIGHTNING_CHASE("LIGHTNING_CHASE", "追电", "速度+30%"),

  // —— 产出系 ——
  HIGH_YIELD("HIGH_YIELD", "高产", "产出量+30%"),
  DILIGENT("DILIGENT", "勤勉", "产出间隔-25%"),
  RARE_PRODUCE("RARE_PRODUCE", "稀产", "5%概率产出稀有物品"),

  // —— 突破系 ——
  SPIRITUAL("SPIRITUAL", "灵悟", "升阶时品质提升概率+10%"),

  // —— 经验系 ——
  SPIRIT_DEVOUR("SPIRIT_DEVOUR", "噬灵", "战斗经验+25%"),

  // —— 恢复系 ——
  SELF_HEAL("SELF_HEAL", "自愈", "战斗结束后恢复10%最大生命值"),

  // —— 繁育系 ——
  FERTILE("FERTILE", "孕灵", "繁育时后代品质提升概率+15%"),
  PROLIFIC("PROLIFIC", "多产", "繁育冷却时间-30%"),
  BLOOD_AWAKEN("BLOOD_AWAKEN", "血脉觉醒", "后代词条继承概率提升至50%"),

  // —— 战斗系 ——
  COUNTER("COUNTER", "反击", "受击时15%概率反击，造成攻击力50%伤害"),
  BLOCK("BLOCK", "格挡", "10%概率减免50%伤害"),
  LIFESTEAL("LIFESTEAL", "吸血", "伤害的10%转化为HP恢复"),
  BERSERK("BERSERK", "狂暴", "HP低于30%时攻击力+50%");

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
