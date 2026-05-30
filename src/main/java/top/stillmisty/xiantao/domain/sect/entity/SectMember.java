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
import top.stillmisty.xiantao.domain.sect.enums.SectPosition;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@EqualsAndHashCode
@Table("xt_sect_member")
@Accessors(chain = true)
@SuppressWarnings("NullAway")
@Data
@NoArgsConstructor
public class SectMember {

  public static SectMember create() {
    return new SectMember();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  @Nullable private Long sectId;

  private Long userId;

  private SectPosition position;

  private Integer contribution;

  @Column(onInsertValue = "now()")
  private LocalDateTime joinedAt;

  @Nullable private LocalDateTime cooldownUntil;

  public boolean isOnCooldown() {
    return cooldownUntil != null && cooldownUntil.isAfter(TimeUtil.now());
  }
}
