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
import top.stillmisty.xiantao.domain.sect.enums.SectPosition;

/** 宗门成员实体 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_sect_member")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class SectMember extends Model<SectMember> {

  public static SectMember create() {
    return new SectMember();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long sectId;

  private Long userId;

  private SectPosition position;

  private Integer contribution;

  @Column(onInsertValue = "now()")
  private LocalDateTime joinedAt;

  private LocalDateTime cooldownUntil;

  public boolean isOnCooldown() {
    return cooldownUntil != null && cooldownUntil.isAfter(LocalDateTime.now());
  }

  public boolean canInvite() {
    return position == SectPosition.LEADER
        || position == SectPosition.VICE_LEADER
        || position == SectPosition.ELDER;
  }

  public boolean canKick() {
    return position == SectPosition.LEADER || position == SectPosition.VICE_LEADER;
  }

  public boolean canPostNotice() {
    return position == SectPosition.LEADER || position == SectPosition.VICE_LEADER;
  }

  public boolean canPublishTask() {
    return position == SectPosition.LEADER
        || position == SectPosition.VICE_LEADER
        || position == SectPosition.ELDER
        || position == SectPosition.ELITE;
  }

  public boolean canManage() {
    return position == SectPosition.LEADER;
  }
}
