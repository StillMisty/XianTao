package top.stillmisty.xiantao.domain.fudi.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.EffectEntryListTypeHandler;
import top.stillmisty.xiantao.service.activity.effect.EffectEntry;

/** 福地事件模板 — 地灵对话懒生成的事件池，机制效果复用 SubEventEffect 参数格式 */
@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("fudi_event_template")
public class FudiEventTemplate {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  /** 事件名称 */
  private String name;

  /** 事件描述（支持 {{key}} 占位符，由机制效果结果填充；无占位符时原样展示） */
  private String description;

  /** 机制效果（与 SubEventEffectExecutor 消费的格式一致，空列表为纯叙事事件） */
  @Column(typeHandler = EffectEntryListTypeHandler.class)
  private List<EffectEntry> effects;

  /** 选取权重 */
  private Integer selectionWeight;

  /** 是否启用 */
  private Boolean enabled;

  @Column(onInsertValue = "now()")
  private LocalDateTime createdAt;

  public int getSelectionWeightInt() {
    return selectionWeight != null ? selectionWeight : 100;
  }
}
