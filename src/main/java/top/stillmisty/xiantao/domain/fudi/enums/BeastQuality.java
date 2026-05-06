package top.stillmisty.xiantao.domain.fudi.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 灵兽品质 */
@Getter
public enum BeastQuality {
  MORTAL("MORTAL", "凡品", 1.0, 1.0, 1.0, 900, 30),
  SPIRIT("SPIRIT", "灵品", 1.2, 0.85, 1.1, 80, 60),
  IMMORTAL("IMMORTAL", "仙品", 1.5, 0.70, 1.2, 18, 120),
  SAINT("SAINT", "圣品", 2.0, 0.55, 1.3, 2, 240),
  DIVINE("DIVINE", "神品", 3.0, 0.40, 1.5, 0, 480);

  @EnumValue private final String code;
  private final String chineseName;
  private final double outputMultiplier;
  private final double auraCostMultiplier;
  private final double lifespanMultiplier;
  private final int hatchWeight;
  private final int recoveryMinutes;

  private final int order;

  BeastQuality(
      String code,
      String chineseName,
      double outputMultiplier,
      double auraCostMultiplier,
      double lifespanMultiplier,
      int hatchWeight,
      int recoveryMinutes) {
    this.code = code;
    this.chineseName = chineseName;
    this.outputMultiplier = outputMultiplier;
    this.auraCostMultiplier = auraCostMultiplier;
    this.lifespanMultiplier = lifespanMultiplier;
    this.hatchWeight = hatchWeight;
    this.recoveryMinutes = recoveryMinutes;
    this.order = ordinal();
  }

  public static BeastQuality fromCode(String code) {
    for (BeastQuality q : values()) {
      if (q.code.equals(code)) {
        return q;
      }
    }
    throw new IllegalArgumentException("Unknown BeastQuality code: " + code);
  }

  public BeastQuality next() {
    return switch (this) {
      case MORTAL -> SPIRIT;
      case SPIRIT -> IMMORTAL;
      case IMMORTAL -> SAINT;
      case SAINT -> DIVINE;
      case DIVINE -> throw new IllegalStateException("神品已是最高品质");
    };
  }
}
