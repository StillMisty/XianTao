package top.stillmisty.xiantao.domain.item.enums;

import lombok.Getter;

/** 法器子类型枚举 */
@Getter
public enum WeaponType {
  BLADE("blade", "刀", "刀兵", "刚猛斩击，克制妖兽"),
  SWORD("sword", "剑", "刀兵", "灵动穿刺，克制灵体"),
  AXE("axe", "斧", "刀兵", "沉重破甲，克制甲胄"),
  SPEAR("spear", "枪", "长兵", "长驱直入，克制猛兽"),
  STAFF("staff", "棍", "长兵", "降妖伏魔，克制邪祟"),
  BOW("bow", "弓", "远兵", "远程精准，克制飞行"),
  WHIP("whip", "鞭", "奇兵", "灵动缠绕"),
  HALBERD("halberd", "戟", "奇兵", "劈刺一体"),
  HAMMER("hammer", "锤", "奇兵", "重击碎魂"),
  DAGGER("dagger", "匕首", "奇兵", "短小精悍"),
  FAN("fan", "扇", "奇兵", "风雷变化"),
  FLYWHISK("flywhisk", "拂尘", "奇兵", "万象归空"),
  RING("ring", "圈", "奇兵", "乾天坤地"),
  BELL("bell", "钟", "奇兵", "震慑心神");

  private final String code;
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
      if (type.code.equalsIgnoreCase(code)) return type;
    }
    return null;
  }
}
