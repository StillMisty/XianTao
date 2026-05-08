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
import top.stillmisty.xiantao.domain.event.enums.GameEventCategory;
import top.stillmisty.xiantao.infrastructure.mybatis.handler.JsonbTypeHandler;

/** 游戏事件实体 — 异步事件队列 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table("xt_game_event")
@Accessors(chain = true)
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

  private String narrativeKey;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> narrativeArgs;

  @Column(typeHandler = JsonbTypeHandler.class)
  private Map<String, Object> effects;

  public static GameEvent create(Long userId, GameEventCategory category) {
    GameEvent event = new GameEvent();
    event.userId = userId;
    event.category = category;
    event.occurredAt = LocalDateTime.now();
    event.delivered = false;
    event.narrativeArgs = Map.of();
    event.effects = Map.of();
    return event;
  }

  public GameEvent withNarrative(String key, Map<String, Object> args) {
    this.narrativeKey = key;
    this.narrativeArgs = args != null ? args : Map.of();
    return this;
  }

  public GameEvent withEffects(Map<String, Object> effects) {
    this.effects = effects != null ? effects : Map.of();
    return this;
  }

  public boolean isUndelivered() {
    return delivered != null && !delivered;
  }
}
