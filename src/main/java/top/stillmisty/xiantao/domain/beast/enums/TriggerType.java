package top.stillmisty.xiantao.domain.beast.enums;

import lombok.Getter;

/** 变异效果触发条件类型 */
@Getter
public enum TriggerType {
  HP_BELOW("HP_BELOW", "生命值低于阈值"),
  ON_KILL("ON_KILL", "击杀时"),
  ON_HIT("ON_HIT", "命中时"),
  ON_ATTACKED("ON_ATTACKED", "被攻击时"),
  ON_DEATH("ON_DEATH", "死亡时"),
  FIRST_N_TURNS("FIRST_N_TURNS", "前N回合");

  private final String code;
  private final String name;

  TriggerType(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static TriggerType fromCode(String code) {
    for (TriggerType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown TriggerType code: " + code);
  }
}
