package top.stillmisty.xiantao.domain.sect.entity;

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

@EqualsAndHashCode
@Table("xt_sect")
@Accessors(chain = true)
@SuppressWarnings("NullAway")
@Data
@NoArgsConstructor
public class Sect {

  public static Sect create() {
    return new Sect();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;

  private Long leaderId;

  private Integer level;

  private Long funds;

  private Integer maxMembers;

  @Nullable private String description;

  @Nullable private String notice;

  @Nullable private String verse;

  @Nullable private String ethos;

  @Nullable private String spiritPersonality;

  @Nullable private String lastEventType;

  @Nullable private String lastEventText;

  @Nullable private LocalDateTime lastEventTime;

  @Nullable private LocalDateTime eventExpiresAt;

  @Nullable private LocalDateTime lastVeinPayout;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updatedAt;

  public boolean isLeader(Long userId) {
    return leaderId.equals(userId);
  }

  public void addFunds(long amount) {
    this.funds = this.funds + amount;
  }

  public boolean deductFunds(long amount) {
    if (this.funds < amount) {
      return false;
    }
    this.funds = this.funds - amount;
    return true;
  }

  public void deductFundsOrThrow(long amount) {
    if (!deductFunds(amount)) {
      throw new top.stillmisty.xiantao.service.BusinessException(
          top.stillmisty.xiantao.service.ErrorCode.SECT_FUNDS_INSUFFICIENT, amount, this.funds);
    }
  }

  public boolean isMaxLevel() {
    return level >= 5;
  }

  public int getScriptureSlotCount(int buildingLevel) {
    return 3 + (buildingLevel * 3);
  }
}
