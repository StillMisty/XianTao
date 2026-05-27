package top.stillmisty.xiantao.domain.beast.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 灵兽性别 — 阴阳 */
@Getter
public enum BeastGender {
  YIN("YIN", "阴", "♀"),
  YANG("YANG", "阳", "♂");

  @EnumValue private final String code;
  private final String chineseName;
  private final String symbol;

  BeastGender(String code, String chineseName, String symbol) {
    this.code = code;
    this.chineseName = chineseName;
    this.symbol = symbol;
  }

  public static BeastGender fromCode(String code) {
    for (BeastGender g : values()) {
      if (g.code.equals(code)) return g;
    }
    throw new IllegalArgumentException("Unknown BeastGender code: " + code);
  }
}
