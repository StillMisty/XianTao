package top.stillmisty.xiantao.domain.event.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

/** 活动事件关联实体 — 子事件/隐藏事件配置 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_activity_event")
@NoArgsConstructor
public class ActivityEvent {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private String activityType;

  private Long ownerId;

  private String code;

  private Integer weight;

  private Boolean isHidden;

  private String triggerType;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> triggerParams;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> params;

  @Column(onInsertValue = "now()")
  private LocalDateTime createTime;

  public boolean isHidden() {
    return isHidden != null && isHidden;
  }

  public int getWeight() {
    return weight != null ? weight : 100;
  }
}
