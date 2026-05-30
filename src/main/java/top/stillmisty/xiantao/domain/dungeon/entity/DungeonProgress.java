package top.stillmisty.xiantao.domain.dungeon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("dungeon_progress")
public class DungeonProgress {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;
  private Long dungeonId;
  private LocalDate lastRewardDate;
  private Integer rewardCount;
  private Integer dailyLimit;
  private Boolean firstClear;

  @Nullable private String bestArea;

  private Integer interactionCount;

  public boolean canGetReward() {
    LocalDate today = TimeUtil.today();
    if (lastRewardDate == null || !lastRewardDate.equals(today)) {
      return true;
    }
    return rewardCount < dailyLimit;
  }

  public void recordReward() {
    LocalDate today = TimeUtil.today();
    if (lastRewardDate == null || !lastRewardDate.equals(today)) {
      lastRewardDate = today;
      rewardCount = 1;
    } else {
      rewardCount++;
    }
  }

  public static int calculateDailyLimit(int playerLevel) {
    if (playerLevel <= 9) return 1;
    if (playerLevel <= 19) return 2;
    if (playerLevel <= 29) return 3;
    return 4;
  }
}
