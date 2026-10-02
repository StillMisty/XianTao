package top.stillmisty.xiantao.domain.notification.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.notification.enums.GameEventCategory;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.EffectDataTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

/** 游戏事件实体 — 异步事件队列 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("game_event")
@Accessors(chain = true)
@SuppressWarnings("NullAway")
@Data
@NoArgsConstructor
public class GameEvent {

  /** 隐藏元数据键：来源活动事件 code（仅程序读取，不参与叙事渲染） */
  public static final String SOURCE_EVENT_CODE_KEY = "source_event_code";

  @EqualsAndHashCode.Include
  @Id(keyType = KeyType.Auto)
  private Long id;

  private Long userId;

  private GameEventCategory category;

  @Column(onInsertValue = "now()")
  private LocalDateTime occurredAt;

  private Boolean delivered;

  @Nullable private String narrativeKey;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> narrativeArgs;

  @Column(value = "effects", typeHandler = EffectDataTypeHandler.class)
  private @Nullable EffectData effectData;

  public static GameEvent create(Long userId, GameEventCategory category) {
    GameEvent event = new GameEvent();
    event.userId = userId;
    event.category = category;
    event.occurredAt = TimeUtil.now();
    event.delivered = false;
    event.narrativeArgs = Map.of();
    event.effectData = null;
    return event;
  }

  public GameEvent withNarrative(String key, @Nullable Map<String, Object> args) {
    this.narrativeKey = key;
    this.narrativeArgs = args != null ? args : Map.of();
    return this;
  }

  public GameEvent withEffectData(@Nullable EffectData effectData) {
    this.effectData = effectData;
    return this;
  }

  /** 记录来源活动事件 code（放在 narrativeArgs 中传递，不渲染给玩家） */
  public GameEvent withSourceEventCode(String eventCode) {
    Map<String, Object> args =
        new java.util.HashMap<>(narrativeArgs != null ? narrativeArgs : Map.of());
    args.put(SOURCE_EVENT_CODE_KEY, eventCode);
    this.narrativeArgs = Map.copyOf(args);
    return this;
  }

  /** 来源活动事件 code；非活动事件来源时返回 null */
  public @Nullable String sourceEventCode() {
    if (narrativeArgs == null) return null;
    Object value = narrativeArgs.get(SOURCE_EVENT_CODE_KEY);
    return value instanceof String code ? code : null;
  }

  public boolean isChoiceEvent() {
    return effectData instanceof EffectData.ChoiceOptions;
  }
}
