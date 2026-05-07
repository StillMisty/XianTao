package top.stillmisty.xiantao.domain.pill.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 玩家 Buff 实体 — 战斗增益和突破加成等时效性 buff */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_player_buff")
public class PlayerBuff {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 用户ID */
  private Long userId;

  /** buff类型：attack/defense/speed/breakthrough */
  private String buffType;

  /** 增益值 */
  private Integer value;

  /** 过期时间 */
  private LocalDateTime expiresAt;

  /** 创建时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  public boolean isExpired() {
    return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
  }

  public boolean isActive() {
    return !isExpired();
  }

  public static PlayerBuff create(
      Long userId, String buffType, int value, LocalDateTime expiresAt) {
    PlayerBuff buff = new PlayerBuff();
    buff.userId = userId;
    buff.buffType = buffType;
    buff.value = value;
    buff.expiresAt = expiresAt;
    buff.createdAt = LocalDateTime.now();
    return buff;
  }
}
