package top.stillmisty.xiantao.domain.beast.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;

/** 灵兽繁育配方实体 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_breeding_recipe")
public class BreedingRecipe {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Integer id;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private List<String> requiredTags;

  private Long resultTemplateId;

  private Integer weight;

  private String description;
}
