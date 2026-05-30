package top.stillmisty.xiantao.domain.beast.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.domain.item.entity.BeastSkillPool;
import top.stillmisty.xiantao.domain.item.entity.ProductionItem;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbCollectionTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_beast_template")
public class BeastTemplate {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;

  private Integer growTime;

  @Column(typeHandler = JsonbTypeHandler.class)
  private List<ProductionItem> productionItems;

  @Column(typeHandler = JsonbTypeHandler.class)
  private BeastSkillPool skillPool;

  @Column(typeHandler = JsonbCollectionTypeHandler.class)
  private Set<String> tags;

  private String description;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;
}
