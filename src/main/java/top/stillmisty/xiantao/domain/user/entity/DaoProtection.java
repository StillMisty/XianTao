package top.stillmisty.xiantao.domain.user.entity;

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

/** 护道关系实体 */
@EqualsAndHashCode(callSuper = true)
@Table("xt_dao_protection")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class DaoProtection extends Model<DaoProtection> {

  public static DaoProtection create() {
    return new DaoProtection();
  }

  /** 护道关系ID */
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 护道者ID (提供加成的一方) */
  private Long protectorId;

  /** 被护道者ID (突破的一方) */
  private Long protegeId;

  /** 建立护道关系的时间 */
  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  /** 更新时间 */
  @Column(onUpdateValue = "now()", onInsertValue = "now()")
  private LocalDateTime updateTime;

  public boolean isProtegeOf(Long userId) {
    return protegeId.equals(userId);
  }

  public boolean isProtectorOf(Long userId) {
    return protectorId.equals(userId);
  }

  public static DaoProtection create(Long protectorId, Long protegeId) {
    DaoProtection daoProtection = new DaoProtection();
    daoProtection.protectorId = protectorId;
    daoProtection.protegeId = protegeId;
    return daoProtection;
  }
}
