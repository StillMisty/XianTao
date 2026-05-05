package top.stillmisty.xiantao.domain.pill.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;

/** 丹药抗性实体 — 记录玩家对每种丹药的服用次数 */
@Data
@Table("xt_pill_resistance")
public class PillResistance {

  /** 代理主键 */
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 用户ID */
  private Long userId;

  /** 丹药模板ID */
  private Long templateId;

  /** 服用次数 */
  private Integer count;

  /** 更新时间 */
  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updatedAt;

  public static PillResistance create(Long userId, Long templateId) {
    PillResistance pr = new PillResistance();
    pr.userId = userId;
    pr.templateId = templateId;
    pr.count = 1;
    pr.updatedAt = LocalDateTime.now();
    return pr;
  }
}
