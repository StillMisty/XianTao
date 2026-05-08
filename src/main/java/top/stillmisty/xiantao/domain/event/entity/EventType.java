package top.stillmisty.xiantao.domain.event.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** 事件类型定义实体 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_event_type")
@NoArgsConstructor
public class EventType {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String activityType;

  private String code;

  private String name;

  private String description;
}
