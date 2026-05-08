package top.stillmisty.xiantao.domain.event.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 隐藏事件完成记录实体 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_hidden_completion")
@NoArgsConstructor
public class HiddenCompletion {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  private String activityType;

  private Long ownerId;

  private String code;

  @Column(onInsertValue = "now()")
  private LocalDateTime completedAt;

  public static HiddenCompletion create(
      Long userId, String activityType, Long ownerId, String code) {
    HiddenCompletion hc = new HiddenCompletion();
    hc.userId = userId;
    hc.activityType = activityType;
    hc.ownerId = ownerId;
    hc.code = code;
    return hc;
  }
}
