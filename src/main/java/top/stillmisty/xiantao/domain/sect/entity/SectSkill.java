package top.stillmisty.xiantao.domain.sect.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** 宗门功法（已解锁） */
@EqualsAndHashCode(callSuper = true)
@Table("xt_sect_skill")
@Accessors(chain = true)
@Data
@NoArgsConstructor
public class SectSkill extends Model<SectSkill> {

  public static SectSkill create() {
    return new SectSkill();
  }

  private Long sectId;

  private Long skillId;

  @Column(onInsertValue = "now()")
  private LocalDateTime unlockedAt;
}
