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
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

/** 福地核心实体 */
@EqualsAndHashCode
@Table("fudi")
@Accessors(chain = true)
@SuppressWarnings("NullAway")
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
  @Nullable private LocalDateTime lastTribulationTime;

  /** 天劫连续胜利次数 */
  private Integer tribulationWinStreak;

  /** 上次福地事件生成时间（地灵对话懒生成节流，至少间隔 4 小时） */
  @Nullable private LocalDateTime lastEventTime;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  // ===================== 业务逻辑方法 =====================

  /** 更新在线时间 */
  public void touchOnlineTime() {
    lastOnlineTime = TimeUtil.now();
  }

  /** 记录本次福地事件生成时间（与条件 UPDATE 的占用共同构成懒生成节流） */
  public void touchEventTime() {
    lastEventTime = TimeUtil.now();
  }
}
