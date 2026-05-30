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

/** 福地核心实体 */
@EqualsAndHashCode
@Table("xt_fudi")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class Fudi {

  public static Fudi create() {
    return new Fudi();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  /** 当前劫数（每渡过一次天劫+1，无上限） */
  private Integer tribulationStage;

  /** 上次上线时间 */
  private LocalDateTime lastOnlineTime;

  /** 天劫最后发生时间 */
  private LocalDateTime lastTribulationTime;

  /** 天劫连续胜利次数 */
  private Integer tribulationWinStreak;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  // ===================== 业务逻辑方法 =====================

  /** 更新在线时间 */
  public void touchOnlineTime() {
    lastOnlineTime = LocalDateTime.now();
  }
}
