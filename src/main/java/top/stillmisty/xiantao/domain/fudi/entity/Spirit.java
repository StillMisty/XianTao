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
import top.stillmisty.xiantao.domain.fudi.enums.MBTIPersonality;

@EqualsAndHashCode
@Table("xt_spirit")
@Accessors(chain = true)
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

  private LocalDateTime lastGiftTime;

  private LocalDateTime lastEventTime;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  public void addAffection(int amount) {
    int maxAff = affectionMax != null ? affectionMax : 1000;
    affection = Math.clamp((affection != null ? affection : 0) + amount, 0, maxAff);
  }
}
