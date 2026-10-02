package top.stillmisty.xiantao.domain.fudi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.fudi.enums.EmotionState;
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;

@EqualsAndHashCode
@Table("spirit")
@Accessors(chain = true)
@SuppressWarnings("NullAway")
@Data
@NoArgsConstructor
public class Spirit {

  public static Spirit create() {
    return new Spirit();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long fudiId;

  private Long formId;

  private Integer affection;

  private Integer affectionMax;

  private MBTIPersonality mbtiType;

  private EmotionState emotionState;

  @Nullable private LocalDateTime lastGiftTime;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  public void addAffection(int amount) {
    int maxAff = affectionMax != null ? affectionMax : 1000;
    affection = Math.clamp((affection != null ? affection : 0) + amount, 0, maxAff);
    updateEmotionState();
  }

  /**
   * 按好感度自动切换情绪状态。
   *
   * <p>自动档位（依恋/愉悦/满足/平和/疏离）由好感度重算；手动/事件状态（忧虑/兴奋/愤怒/虚弱）保留， 不被自动切换覆盖，直到下一次显式设置（LLM 工具或天劫事件）。
   */
  public void updateEmotionState() {
    EmotionState current = emotionState;
    if (current != null && !current.isAffectionDriven()) {
      return;
    }
    emotionState = EmotionState.fromAffection(affection != null ? affection : 0);
  }
}
