package top.stillmisty.xiantao.domain.skill.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_player_skill")
public class PlayerSkill {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  private Long skillId;

  private Boolean isEquipped;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  public boolean isEquipped() {
    return isEquipped != null && isEquipped;
  }

  public void equip() {
    isEquipped = true;
  }

  public void unequip() {
    isEquipped = false;
  }

  public static PlayerSkill create(Long userId, Long skillId, boolean equipped) {
    PlayerSkill playerSkill = new PlayerSkill();
    playerSkill.userId = userId;
    playerSkill.skillId = skillId;
    playerSkill.isEquipped = equipped;
    return playerSkill;
  }
}
