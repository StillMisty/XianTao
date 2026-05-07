package top.stillmisty.xiantao.domain.bounty.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.bounty.BountyRewardItem;
import top.stillmisty.xiantao.domain.bounty.enums.BountyStatus;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.BountyRewardListTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_user_bounty")
public class UserBounty {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  private Long bountyId;

  private String bountyName;

  private LocalDateTime startTime;

  private Integer durationMinutes;

  @Column(typeHandler = BountyRewardListTypeHandler.class)
  private List<BountyRewardItem> rewards;

  private BountyStatus status;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  public boolean isActive() {
    return status == BountyStatus.ACTIVE;
  }

  public boolean isCompleted() {
    return status == BountyStatus.COMPLETED;
  }

  public List<BountyRewardItem> getParsedRewardItems() {
    return rewards != null ? rewards : List.of();
  }
}
