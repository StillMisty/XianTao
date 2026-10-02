package top.stillmisty.xiantao.domain.analytics.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

/** 分析用原始行为事件（append-only；事件字典见 tools/analytics/README.md）。 */
@SuppressWarnings("NullAway")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("analytics_event")
public class AnalyticsEvent {

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  @Column(onInsertValue = "now()")
  private LocalDateTime occurredAt;

  /** 关联玩家；系统级事件可为空 */
  @Nullable private Long userId;

  /** 事件类型（字典见 README） */
  private String kind;

  /** 主体：地图/怪物/指令/来源等 */
  @Nullable private String subject;

  /** 主数值：经验/灵石/回合/耗时等 */
  @Nullable private Long value;

  /** 扩展字段（JSONB，不参与渲染） */
  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> payload = new LinkedHashMap<>();
}
