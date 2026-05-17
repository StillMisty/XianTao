package top.stillmisty.xiantao.domain.masterapprentice.entity;

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
import top.stillmisty.xiantao.domain.masterapprentice.enums.MasterApprenticeStatus;

/** 师徒关系实体 */
@EqualsAndHashCode(callSuper = true)
@Table("master_apprentice")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class MasterApprentice extends Model<MasterApprentice> {

  public static MasterApprentice create() {
    return new MasterApprentice();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long masterId;

  private Long apprenticeId;

  private MasterApprenticeStatus status;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  private LocalDateTime graduatedAt;

  private LocalDateTime cooldownUntil;

  public boolean isActive() {
    return status == MasterApprenticeStatus.ACTIVE;
  }

  public static MasterApprentice create(Long masterId, Long apprenticeId) {
    MasterApprentice ma = new MasterApprentice();
    ma.masterId = masterId;
    ma.apprenticeId = apprenticeId;
    ma.status = MasterApprenticeStatus.ACTIVE;
    return ma;
  }
}
