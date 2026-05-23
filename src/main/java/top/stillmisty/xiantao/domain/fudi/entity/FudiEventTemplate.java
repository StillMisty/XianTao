package top.stillmisty.xiantao.domain.fudi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table("fudi_event_template")
public class FudiEventTemplate {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String name;

  private String description;

  @Column(typeHandler = JsonbTypeHandler.class)
  private List<Map<String, Object>> effects;

  private Integer selectionWeight;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  private String createdBy;

  public boolean hasEffects() {
    return effects != null && !effects.isEmpty();
  }
}
