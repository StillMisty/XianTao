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
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import top.stillmisty.xiantao.domain.event.EffectData;
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.EffectDataTypeHandler;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;
import top.stillmisty.xiantao.infrastructure.util.TimeUtil;

/** 游戏事件实体 — 异步事件队列 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_game_event")
@Accessors(chain = true)
@SuppressWarnings("NullAway")
@Data
@NoArgsConstructor
public class GameEvent {

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

  public boolean isChoiceEvent() {
    return effectData instanceof EffectData.ChoiceOptions;
  }
}
