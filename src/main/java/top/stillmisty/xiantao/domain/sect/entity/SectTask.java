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
import top.stillmisty.xiantao.domain.sect.enums.SectTaskType;

@EqualsAndHashCode
@Table("xt_sect_task")
@Accessors(chain = true)
@Data
@SuppressWarnings("NullAway")
@NoArgsConstructor
public class SectTask {

  public static SectTask create() {
    return new SectTask();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long sectId;

  private SectTaskType taskType;

  private Long targetId;

  private Integer requiredCount;

  private Integer contributionReward;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;
}
