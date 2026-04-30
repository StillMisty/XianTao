package top.stillmisty.xiantao.domain.skill.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import top.stillmisty.xiantao.domain.skill.enums.BindingType;
import top.stillmisty.xiantao.domain.skill.enums.EffectType;
import top.stillmisty.xiantao.domain.skill.enums.SkillType;

import java.time.LocalDateTime;

@Data
@Table("xt_skill")
public class Skill {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String name;

    private String description;

    private SkillType skillType;

    private EffectType effectType;

    private BindingType bindingType;

    private String bindingValue;

    private Integer cooldownSeconds;

    private String damageFormula;

    private Double powerMultiplier;

    private Integer levelRequirement;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
