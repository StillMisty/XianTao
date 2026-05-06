package top.stillmisty.xiantao.domain.bounty.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import top.stillmisty.xiantao.domain.bounty.BountyRewardPool;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.BountyRewardListTypeHandler;

/** 悬赏任务实体 */
@Data
@Table("xt_bounty")
public class Bounty {

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long mapId;

  private String name;

  private String description;

  private Integer durationMinutes;

  @Column(typeHandler = BountyRewardListTypeHandler.class)
  private List<BountyRewardPool> rewards;

  private Integer requireLevel;

  private Integer eventWeight;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  public boolean requiresLevel(int userLevel) {
    return requireLevel == null || userLevel >= requireLevel;
  }

  public boolean hasRewards() {
    return rewards != null && !rewards.isEmpty();
  }

  public int getTotalEventWeight() {
    if (rewards == null) return 0;
    return rewards.stream().mapToInt(BountyRewardPool::weight).sum();
  }
}
