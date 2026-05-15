package top.stillmisty.xiantao.domain.pill.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 丹药抗性实体 — 记录玩家对每种丹药各品质的服用次数 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_pill_resistance")
public class PillResistance {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 用户ID */
  private Long userId;

  /** 丹药模板ID */
  private Long templateId;

  /** 丹药品质 */
  private String quality;

  /** 服用次数 */
  private Integer count;

  /** 更新时间 */
  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updatedAt;

  public static PillResistance create(Long userId, Long templateId, String quality) {
    PillResistance pr = new PillResistance();
    pr.userId = userId;
    pr.templateId = templateId;
    pr.quality = quality;
    pr.count = 1;
    pr.updatedAt = LocalDateTime.now();
    return pr;
  }
}
