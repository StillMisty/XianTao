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
@Table("sect")
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

  /** UPDATE 时不自写，资金变动统一由 SectLedger 原子 SQL 控制 */
  @Column(onUpdateValue = "funds")
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

  public boolean isMaxLevel() {
    return level >= 5;
  }

  public int getScriptureSlotCount(int buildingLevel) {
    return 3 + (buildingLevel * 3);
  }
}
