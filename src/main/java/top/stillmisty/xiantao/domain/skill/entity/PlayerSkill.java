package top.stillmisty.xiantao.domain.skill.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Table("xt_player_skill")
public class PlayerSkill {

  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  private Long skillId;

  private Boolean isEquipped;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;
}
