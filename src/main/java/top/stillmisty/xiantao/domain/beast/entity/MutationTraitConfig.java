package top.stillmisty.xiantao.domain.beast.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

/** 灵兽变异特性配置实体 */
@Data
@NoArgsConstructor
@Table("xt_mutation_trait_config")
public class MutationTraitConfig {

  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;

  private String chineseName;

  private String description;

  private String category;

  @Column(typeHandler = JsonbTypeHandler.class)
  private List<MutationEffect> effects;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<String> requiredTags;

  private String requiredQuality;

  private Boolean isActive;

  private Integer sortOrder;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;
}
