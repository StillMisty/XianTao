package top.stillmisty.xiantao.domain.fudi.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 地灵情绪状态（code 与 spirit.emotion_state CHECK 约束一致） */
@Getter
public enum EmotionState {
  AFFECTIONATE("AFFECTIONATE", "依恋", "满心依恋，语气亲昵温柔，把主人当作最重要的人"),
  JOYFUL("JOYFUL", "愉悦", "心情愉悦，语气轻快，乐于主动帮忙"),
  CONTENT("CONTENT", "满足", "心境满足平和，语气温和有礼"),
  NEUTRAL("NEUTRAL", "平和", "情绪平稳，语气自然如常"),
  DISTANT("DISTANT", "疏离", "心存疏离，语气生淡，不愿多言"),
  WORRIED("WORRIED", "忧虑", "忧心忡忡，语气里带着不安与牵挂"),
  EXCITED("EXCITED", "兴奋", "兴奋雀跃，语气高昂，话也变多"),
  ANGRY("ANGRY", "愤怒", "余怒未消，语气带刺，说话不客气"),
  EXHAUSTED("EXHAUSTED", "虚弱", "精力耗尽，语气虚弱疲惫，说话有气无力");

  /** 枚举参数描述（供 @ToolParam 引用） */
  public static final String PARAM_DESCRIPTION =
      "情绪状态: AFFECTIONATE(依恋) JOYFUL(愉悦) CONTENT(满足) NEUTRAL(平和) DISTANT(疏离)"
          + " WORRIED(忧虑) EXCITED(兴奋) ANGRY(愤怒) EXHAUSTED(虚弱)";

  @EnumValue private final String code;
  private final String chineseName;
  private final String toneHint;

  EmotionState(String code, String chineseName, String toneHint) {
    this.code = code;
    this.chineseName = chineseName;
    this.toneHint = toneHint;
  }

  public static EmotionState fromCode(String code) {
    for (EmotionState state : values()) {
      if (state.code.equals(code)) {
        return state;
      }
    }
    throw new IllegalArgumentException("Unknown EmotionState code: " + code);
  }

  /**
   * 是否为好感度自动档位状态。
   *
   * <p>自动档位（依恋/愉悦/满足/平和/疏离）由 {@code Spirit.updateEmotionState()} 按好感重算；
   * 其余四态（忧虑/兴奋/愤怒/虚弱）视为手动/事件状态，自动切换不会覆盖。
   */
  public boolean isAffectionDriven() {
    return this == AFFECTIONATE
        || this == JOYFUL
        || this == CONTENT
        || this == NEUTRAL
        || this == DISTANT;
  }

  /** 好感度自动档位：≥800 依恋 / ≥500 愉悦 / ≥200 满足 / ≥50 平和 / 否则疏离 */
  public static EmotionState fromAffection(int affection) {
    if (affection >= 800) return AFFECTIONATE;
    if (affection >= 500) return JOYFUL;
    if (affection >= 200) return CONTENT;
    if (affection >= 50) return NEUTRAL;
    return DISTANT;
  }
}
