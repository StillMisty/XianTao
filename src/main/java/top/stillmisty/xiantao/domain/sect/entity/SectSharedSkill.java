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
import top.stillmisty.xiantao.domain.sect.enums.SectSharedSkillStatus;

@EqualsAndHashCode(callSuper = true)
@Table("xt_sect_shared_skill")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class SectSharedSkill extends Model<SectSharedSkill> {

  public static SectSharedSkill create() {
    return new SectSharedSkill();
  }

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long sectId;

  private Long skillId;

  private Long submitterUserId;

  private SectSharedSkillStatus status;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;
}
