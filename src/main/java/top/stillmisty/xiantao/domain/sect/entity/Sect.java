package top.stillmisty.xiantao.domain.sect.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Table("xt_sect")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class Sect extends Model<Sect> {

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

  private String description;

  private String notice;

  private String verse;

  private String ethos;

  private String spiritPersonality;

  private String lastEventType;

  private String lastEventText;

  private LocalDateTime lastEventTime;

  private LocalDateTime eventExpiresAt;

  private LocalDateTime lastVeinPayout;

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
      return true;
    }
    this.funds = this.funds - amount;
    return false;
  }

  public boolean isMaxLevel() {
    return level >= 5;
  }

  public int getScriptureSlotCount(int buildingLevel) {
    return 3 + (buildingLevel * 3);
  }
}
