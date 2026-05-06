package top.stillmisty.xiantao.domain.pill.enums;

import lombok.Getter;

/** 丹药成色 */
@Getter
public enum PillQuality {
  SUPERIOR("SUPERIOR", "上成", 1.5),
  NORMAL("NORMAL", "中成", 1.0),
  INFERIOR("INFERIOR", "下成", 0.7);

  private final String code;
  private final String chineseName;
  private final double multiplier;

  PillQuality(String code, String chineseName, double multiplier) {
    this.code = code;
    this.chineseName = chineseName;
    this.multiplier = multiplier;
  }

  public static PillQuality fromCode(String code) {
    for (PillQuality q : values()) {
      if (q.code.equals(code)) {
        return q;
      }
    }
    throw new IllegalArgumentException("Unknown PillQuality code: " + code);
  }

  public static PillQuality determine(double score) {
    if (score >= 0.8) return SUPERIOR;
    if (score >= 0.5) return NORMAL;
    return INFERIOR;
  }
}
