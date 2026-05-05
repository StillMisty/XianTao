package top.stillmisty.xiantao.domain.skill.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;
import top.stillmisty.xiantao.domain.skill.enums.BindingType;
import top.stillmisty.xiantao.domain.skill.enums.SkillType;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

@Data
@Table("xt_skill")
public class Skill {

  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;

  private String description;

  private SkillType skillType;

  /** 效果列表（JSONB） 支持多效果组合，每个效果有独立参数 */
  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<SkillEffect> effects;

  private BindingType bindingType;

  private String bindingValue;

  private Integer cooldownSeconds;

  private Integer requireWis;

  private Long requireSkillId;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<String> tags;

  private Integer levelRequirement;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  @Column(onUpdateValue = "now()")
  private LocalDateTime updateTime;
}
