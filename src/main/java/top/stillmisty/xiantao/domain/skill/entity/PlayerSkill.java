package top.stillmisty.xiantao.domain.skill.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("xt_player_skill")
public class PlayerSkill {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private Long skillId;

    private Boolean isEquipped;

    private LocalDateTime createTime;
}
