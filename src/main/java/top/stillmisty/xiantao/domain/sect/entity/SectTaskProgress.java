package top.stillmisty.xiantao.domain.sect.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@EqualsAndHashCode
@Table("xt_sect_task_progress")
@Accessors(chain = true)
@Data
@SuppressWarnings("NullAway")
@NoArgsConstructor
public class SectTaskProgress {

  public static SectTaskProgress create() {
    return new SectTaskProgress();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long taskId;

  private Long userId;

  private Integer progress;

  private Boolean completed;

  private LocalDateTime completedAt;
}
