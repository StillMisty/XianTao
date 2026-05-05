package top.stillmisty.xiantao.domain.fudi.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/** 地灵情绪状态 */
@Getter
public enum EmotionState {
  AFFECTIONATE("affectionate", "💕", "与你心意相通，寸步不愿离开"),
  JOYFUL("joyful", "😊", "心情舒畅，觉得跟着你是天大的福分"),
  CONTENT("content", "😌", "对现状颇为满意，岁月静好"),
  NEUTRAL("neutral", "😐", "心如止水，与你客气相处"),
  DISTANT("distant", "😶", "与你尚有隔阂，态度疏离"),
  WORRIED("worried", "😰", "为福地荒芜感到忧虑，盼你多加照料"),
  EXCITED("excited", "🥳", "欣喜若狂，有好事降临"),
  ANGRY("angry", "💢", "天劫失利，迁怒于你的疏忽"),
  EXHAUSTED("exhausted", "😵", "替你挡下天劫，元气大伤陷入沉睡");

  @EnumValue private final String code;
  private final String emoji;
  private final String description;

  EmotionState(String code, String emoji, String description) {
    this.code = code;
    this.emoji = emoji;
    this.description = description;
  }

  public static EmotionState fromCode(String code) {
    for (EmotionState state : values()) {
      if (state.code.equals(code)) {
        return state;
      }
    }
    throw new IllegalArgumentException("Unknown EmotionState code: " + code);
  }
}
