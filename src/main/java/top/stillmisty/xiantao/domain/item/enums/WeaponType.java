package top.stillmisty.xiantao.domain.item.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 法器子类型枚举 */
@Getter
public enum WeaponType {
  BLADE("BLADE", "刀", "刀兵", "刚猛斩击，克制妖兽"),
  SWORD("SWORD", "剑", "刀兵", "灵动穿刺，克制灵体"),
  AXE("AXE", "斧", "刀兵", "沉重破甲，克制甲胄"),
  SPEAR("SPEAR", "枪", "长兵", "长驱直入，克制猛兽"),
  STAFF("STAFF", "棍", "长兵", "降妖伏魔，克制邪祟"),
  BOW("BOW", "弓", "远兵", "远程精准，克制飞行"),
  WHIP("WHIP", "鞭", "奇兵", "灵动缠绕"),
  HALBERD("HALBERD", "戟", "奇兵", "劈刺一体"),
  HAMMER("HAMMER", "锤", "奇兵", "重击碎魂"),
  DAGGER("DAGGER", "匕首", "奇兵", "短小精悍"),
  FAN("FAN", "扇", "奇兵", "风雷变化"),
  FLYWHISK("FLYWHISK", "拂尘", "奇兵", "万象归空"),
  RING("RING", "圈", "奇兵", "乾天坤地"),
  BELL("BELL", "钟", "奇兵", "震慑心神");

  @EnumValue private final String code;
  private final String name;
  private final String category;
  private final String description;

  WeaponType(String code, String name, String category, String description) {
    this.code = code;
    this.name = name;
    this.category = category;
    this.description = description;
  }

  public static WeaponType fromCode(String code) {
    for (WeaponType type : values()) {
      if (type.code.equals(code)) return type;
    }
    throw new IllegalArgumentException("Unknown WeaponType code: " + code);
  }
}
