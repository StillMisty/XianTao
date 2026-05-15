package top.stillmisty.xiantao.domain.team.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.stillmisty.xiantao.domain.team.enums.TeamStatus;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(chain = true)
@Table("team")
public class Team {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long leaderId;
  private Integer memberCount;
  private TeamStatus status;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  public boolean isActive() {
    return status == TeamStatus.ACTIVE;
  }

  public boolean isDisbanded() {
    return status == TeamStatus.DISBANDED;
  }

  public void disband() {
    status = TeamStatus.DISBANDED;
  }

  public void incrementMemberCount() {
    if (memberCount == null) memberCount = 1;
    else memberCount++;
  }

  public void decrementMemberCount() {
    if (memberCount != null && memberCount > 0) memberCount--;
  }

  public boolean isEmpty() {
    return memberCount == null || memberCount <= 0;
  }
}
