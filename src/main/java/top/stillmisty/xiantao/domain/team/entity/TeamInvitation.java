package top.stillmisty.xiantao.domain.team.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.team.enums.InvitationStatus;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(chain = true)
@Table("team_invitation")
public class TeamInvitation {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long teamId;
  private Long inviterId;
  private Long inviteeId;
  private InvitationStatus status;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  private LocalDateTime expiresAt;

  public boolean isPending() {
    return status == InvitationStatus.PENDING;
  }

  public boolean isExpired() {
    return expiresAt != null && TimeUtil.now().isAfter(expiresAt);
  }

  public void accept() {
    status = InvitationStatus.ACCEPTED;
  }

  public void reject() {
    status = InvitationStatus.REJECTED;
  }

  public void expire() {
    status = InvitationStatus.EXPIRED;
  }
}
